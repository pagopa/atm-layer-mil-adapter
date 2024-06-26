package it.gov.pagopa.miladapter.services;

import camundajar.impl.com.google.gson.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.model.ParentSpanContext;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.variable.ClientValues;
import org.camunda.bpm.client.variable.value.JsonValue;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.spin.json.SpinJsonNode;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.camunda.spin.Spin.JSON;

public interface GenericRestService  {

    default VariableMap executeRestCall(Configuration configuration) {
        ResponseEntity<String> response;
        getLogger().info("Start span requestId: ", configuration.getHeaders().get("requestId"));
        SpanBuilder spanBuilder = this.spanBuilder(configuration);
        Span serviceSpan = spanBuilder.startSpan();

        long asyncTimeout = getRestConfigurationProperties().getAsyncThreshold();
        try (Scope scope = serviceSpan.makeCurrent()) {
            if (configuration.getDelayMilliseconds() != null) {
                if (configuration.getDelayMilliseconds() > asyncTimeout / 2) {
                    throw new RuntimeException(String.format("The delay between consecutive retries must be lower than: %s ms", asyncTimeout / 2));
                }
                try {
                    Thread.sleep(configuration.getDelayMilliseconds());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            getLogger().info("End span requestId: ", configuration.getHeaders().get("requestId"));
            getLogger().info("Start get token requestId: ", configuration.getHeaders().get("requestId"));
            this.injectAuthToken(configuration);
            getLogger().info("End get token requestId: ", configuration.getHeaders().get("requestId"));
            getLogger().info("Start create call request requestId: ", configuration.getHeaders().get("requestId"));
            URI url = this.prepareUri(configuration);
            HttpEntity<String> entity = this.buildHttpEntity(configuration);
            serviceSpan.setAttribute(SemanticAttributes.HTTP_METHOD, configuration.getHttpMethod().name());
            serviceSpan.setAttribute(SemanticAttributes.HTTP_URL, url.toString());
            if (entity.hasBody()) {
                serviceSpan.setAttribute("http.body", entity.getBody());
            }
            serviceSpan.setAttribute("http.headers", entity.getHeaders().toString());
            getLogger().info("Stop create call request requestId: ", configuration.getHeaders().get("requestId"));
            getLogger().info("Start rest call requestId: ", configuration.getHeaders().get("requestId"));
            response = this.getRestTemplate(configuration)
                    .exchange(url, configuration.getHttpMethod(), entity, String.class);
            getLogger().info("End rest call requestId: ", configuration.getHeaders().get("requestId"));
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            getLogger().error("Exception in HTTP request: ", e);
            response = new ResponseEntity<>(new JsonObject().toString(), e.getStatusCode());
            serviceSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, e.getStatusCode().value());
            serviceSpan.setAttribute("http.response.body", e.getResponseBodyAsString());
        } catch (Exception e) {
            getLogger().error("Exception in HTTP request: ", e);
            response = new ResponseEntity<>(new JsonObject().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        getLogger().info("Start mapping response requestId: ", configuration.getHeaders().get("requestId"));
        VariableMap output = Variables.createVariables();
        if (response.getBody() == null) {
            response = new ResponseEntity<>(new JsonObject().toString(), response.getStatusCode());
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
        getLogger().info("Stop mapping response requestId: ", configuration.getHeaders().get("requestId"));
        return output;
    }

    void injectAuthToken(Configuration configuration);

    URI prepareUri(Configuration configuration);

    Logger getLogger();

    Tracer getTracer();

    RestConfigurationProperties getRestConfigurationProperties();

    RestTemplateGenerator getRestTemplateGenerator();

    <T> HttpEntity<T> buildHttpEntity(Configuration configuration);

    default RestTemplate getRestTemplate(Configuration configuration) {
        HttpRequestUtils.getRestFactoryConfigsOrDefault(configuration, getRestConfigurationProperties());
        return getRestTemplateGenerator().generate(configuration.getConnectionRequestTimeoutMilliseconds(), configuration.getConnectionResponseTimeoutMilliseconds(),
                configuration.getMaxRetry(), configuration.getRetryIntervalMilliseconds());
    }

    default SpanBuilder spanBuilder(Configuration configuration) {

        String parentSpanContextString = configuration.getParentSpanContextString();
        ParentSpanContext parentSpanContext = new ParentSpanContext();
        if (StringUtils.isNotBlank(parentSpanContextString)) {
            ObjectMapper mapper = new ObjectMapper();

            try {
                parentSpanContext = mapper.readValue(parentSpanContextString, ParentSpanContext.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        SpanBuilder spanBuilder = getTracer().spanBuilder("MIL-Adapter-RestCall-Execution");
        if (parentSpanContext.isNotNull()) {
            SpanContext parentContext = SpanContext.createFromRemoteParent(parentSpanContext.getTraceId(), parentSpanContext.getSpanId(), TraceFlags.getSampled(), TraceState.getDefault());
            Span parentSpan = Span.wrap(parentContext);
            return spanBuilder.setParent(Context.current().with(parentSpan));
        }
        return spanBuilder;
    }
}
