package it.gov.pagopa.miladapter.services.impl;

import camundajar.impl.com.google.gson.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
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
import it.gov.pagopa.miladapter.services.MILRestExternalService;
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
            // Logica del task asincrono
            System.out.println("Inizio task asincrono...");
            try {
                // Simulazione di un compito che richiede tempo
                Configuration configuration = EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCallNew(body);
                VariableMap response = callExternalService(configuration);
                callBackEngine(body, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Task asincrono completato!");
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

    @Autowired
    ObjectMapper objectMapper;

    public ResponseEntity<String> executeCallAndCallBack (Map<String, Object> body) throws JsonProcessingException {
        Configuration configuration = EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCallNew(body);
        VariableMap variableMap = callExternalService(configuration);
        objectMapper.writeValueAsString(variableMap);

        return new ResponseEntity(objectMapper.writeValueAsString(variableMap), HttpStatus.OK);

    }

    private void callBackEngine(Map<String, Object> body, VariableMap response) {
        try {
            CamundaWaitMessage camundaWaitMessage = createCallbackPayload(body, response);
            callbackCamundaService.callAdapter(camundaWaitMessage);
        } catch (Exception e) {
            log.error("Error while invoking callback", e);
        }
    }

    private  VariableMap callExternalService(Configuration configuration) {
        ResponseEntity<String> response;
        VariableMap output = Variables.createVariables();
        SpanBuilder spanBuilder = this.spanBuilder(configuration);
        Span serviceSpan = spanBuilder.startSpan();
        try (Scope scope = serviceSpan.makeCurrent()){
            URI url = this.prepareUri(configuration);
            HttpEntity<String> entity = HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
            serviceSpan.setAttribute(SemanticAttributes.HTTP_METHOD, configuration.getHttpMethod().name());
            serviceSpan.setAttribute(SemanticAttributes.HTTP_URL, url.toString());
            if (entity.hasBody()) {
                serviceSpan.setAttribute("http.body", entity.getBody());
            }
            serviceSpan.setAttribute("http.headers", entity.getHeaders().toString());
            response = this.getRestTemplate(configuration).exchange(url, configuration.getHttpMethod(), entity, String.class);
            if (response.getBody() == null) {
                response = new ResponseEntity<>(new JsonObject().toString(), response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            getLogger().error("Exception in HTTP request: {}", e);
            response = new ResponseEntity<>(new JsonObject().toString(), e.getStatusCode());
            serviceSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, e.getStatusCode().value());
            serviceSpan.setAttribute("http.response.body", e.getResponseBodyAsString());
        } catch (Exception e) {
            getLogger().error("Exception in HTTP request: {}", e);
            response = new ResponseEntity<>(new JsonObject().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        serviceSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, response.getStatusCode().value());
        serviceSpan.setAttribute("http.response.body", response.getBody());
        serviceSpan.setAttribute("http.response.headers", response.getHeaders().toString());

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
        serviceSpan.end();
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
