package it.gov.pagopa.miladapter.services.impl;

import camundajar.impl.com.google.gson.JsonObject;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import it.gov.pagopa.miladapter.dto.CamundaWaitMessage;
import it.gov.pagopa.miladapter.dto.ProcessVariable;
import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.CallbackCamundaService;
import it.gov.pagopa.miladapter.services.ExternalCallService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.variable.ClientValues;
import org.camunda.bpm.client.variable.value.JsonValue;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.camunda.spin.Spin.JSON;

@Slf4j
@Service
public class ExternalCallServiceImpl extends GenericRestExternalServiceAbstract implements ExternalCallService {
    @Qualifier("taskExecutor")
    @Autowired
    private Executor taskExecutor;

    public CompletableFuture<Void> executeAsyncTask(Map<String, Object> body) {
        return CompletableFuture.runAsync(() -> {
            Configuration configuration = EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCallNew(body);
            SpanBuilder parentSpanBuilder = this.spanBuilder(configuration);
            Span parentSpan = parentSpanBuilder.startSpan();
            try (Scope scope = parentSpan.makeCurrent()) {
                // Logica del task asincrono
                System.out.println("Inizio task asincrono...");
                // Simulazione di un compito che richiede tempo
                VariableMap response = callExternalService(configuration);
                callBackEngine(body, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Task asincrono completato!");
            parentSpan.end();
        }, taskExecutor);
    }

    @Override
    public URI prepareUri(Configuration configuration) {
        return HttpRequestUtils.buildURI(restConfigurationProperties.getMilBasePath(), configuration.getEndpoint(), configuration.getPathParams());
    }

    @Override
    public RestTemplate getRestTemplate(Configuration configuration) {
        return super.getRestTemplate(configuration);
    }

    @Override
    public SpanBuilder spanBuilder(Configuration configuration) {
        return super.spanBuilder(configuration);
    }

    @Autowired
    RestConfigurationProperties restConfigurationProperties;
    @Autowired
    RestTemplateGenerator restTemplateGenerator;
    @Autowired
    CallbackCamundaService callbackCamundaService;

    @Async
    public void executeCallAndCallBack(Map<String, Object> body) {
        Configuration configuration = EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCallNew(body);
        VariableMap response = callExternalService(configuration);
        callBackEngine(body, response);
    }

    private void callBackEngine(Map<String, Object> body, VariableMap response) {
        log.info("starting callback");
        SpanBuilder callbackSpanBuilder = getTracer().spanBuilder("Camunda callback");
        callbackSpanBuilder.setParent(Context.current().with(Span.current()));
        Span callBackSpan = callbackSpanBuilder.startSpan();
        try (Scope scope = callBackSpan.makeCurrent()) {
            CamundaWaitMessage camundaWaitMessage = createCallbackPayload(body, response);
            callBackSpan.setAttribute("camunda.wait.message", camundaWaitMessage.toString());
            callBackSpan.setAttribute("start.time", (LocalDateTime.now()).toString());
            callbackCamundaService.callAdapter(camundaWaitMessage);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            getLogger().error("Exception in Camunda callback: {}", e);
            callBackSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, e.getStatusCode().value());
            callBackSpan.setAttribute("http.response.body", e.getResponseBodyAsString());
        } catch (Exception e) {
            getLogger().error("Exception in Camunda callback: {}", e);
            callBackSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, 500);
        }
        callBackSpan.setAttribute("end.time", (LocalDateTime.now()).toString());
        callBackSpan.end();
    }

    private VariableMap callExternalService(Configuration configuration) {
        ResponseEntity<String> response;
        VariableMap output = Variables.createVariables();
        SpanBuilder extSpanBuilder = getTracer().spanBuilder("MIL call");
        extSpanBuilder.setParent(Context.current().with(Span.current()));
        Span extSpan = extSpanBuilder.startSpan();
        URI url = this.prepareUri(configuration);
        HttpEntity<String> entity = HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
        try (Scope scope = extSpan.makeCurrent()) {
            extSpan.setAttribute(SemanticAttributes.HTTP_METHOD, configuration.getHttpMethod().name());
            extSpan.setAttribute(SemanticAttributes.HTTP_URL, url.toString());
            if (entity.hasBody()) {
                extSpan.setAttribute("http.body", entity.getBody());
            }
            extSpan.setAttribute("http.headers", entity.getHeaders().toString());
            extSpan.setAttribute("start.time", (LocalDateTime.now()).toString());
            response = this.getRestTemplate(configuration).exchange(url, configuration.getHttpMethod(), entity, String.class);
            extSpan.setAttribute("end.time", (LocalDateTime.now()).toString());
            if (response.getBody() == null) {
                response = new ResponseEntity<>(new JsonObject().toString(), response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            getLogger().error("Exception in HTTP request: {}", e);
            response = new ResponseEntity<>(new JsonObject().toString(), e.getStatusCode());
            extSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, e.getStatusCode().value());
            extSpan.setAttribute("http.response.body", e.getResponseBodyAsString());
        } catch (Exception e) {
            getLogger().error("Exception in HTTP request: {}", e);
            response = new ResponseEntity<>(new JsonObject().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            extSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, response.getStatusCode().value());
        }
        extSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, response.getStatusCode().value());
        extSpan.setAttribute("http.response.body", response.getBody());
        extSpan.setAttribute("http.response.headers", response.getHeaders().toString());
        JsonValue jsonValue;
        if (StringUtils.isNotBlank(response.getBody())
                && response.getStatusCode() != null
                && response.getStatusCode().is2xxSuccessful()) {
            jsonValue = ClientValues.jsonValue(response.getBody());
        } else {
            jsonValue = ClientValues.jsonValue("{}");
        }
        output.putValue(HttpVariablesEnum.RESPONSE.getValue(), jsonValue);
        output.putValue(HttpVariablesEnum.STATUS_CODE.getValue(), response.getStatusCode().value());
        SpinJsonNode headersJsonNode = JSON(response.getHeaders());
        output.putValue(HttpVariablesEnum.RESPONSE_HEADERS.getValue(), headersJsonNode.toString());
        extSpan.end();
        return output;
    }

    private CamundaWaitMessage createCallbackPayload(Map<String, Object> body, VariableMap response) {
        CamundaWaitMessage camundaWaitMessage = new CamundaWaitMessage();
        camundaWaitMessage.setMessageName((String) body.get("responseEventMessage"));
        camundaWaitMessage.setProcessInstanceId((String) body.get("processInstanceId"));
        String responseBody = response.getValueTyped("response").getValue().toString();
        String statusCode = response.getValueTyped("statusCode").getValue().toString();
        camundaWaitMessage.setProcessVariables(Map.of(
                "statusCode", new ProcessVariable(statusCode, "String"),
                "response", new ProcessVariable(responseBody, "json")
        ));
        return camundaWaitMessage;
    }

    @Override
    public VariableMap executeRestCall(Configuration configuration) {
        return super.executeRestCall(configuration);
    }

    @Override
    public void injectAuthToken(Configuration configuration) {
    }

    public HttpEntity<String> buildHttpEntity(Configuration configuration) {
        return HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
    }
}
