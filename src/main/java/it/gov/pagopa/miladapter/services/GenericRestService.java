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
import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.model.ParentSpanContext;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Date;

import static org.camunda.spin.Spin.JSON;

public interface GenericRestService {

    default VariableMap executeRestCall(Configuration configuration) {

        this.injectAuthToken(configuration);
        ResponseEntity<String> response;
        SpanBuilder spanBuilder = this.spanBuilder(configuration);
        Span serviceSpan = spanBuilder.startSpan();

        long asyncTimeout  = getRestConfigurationProperties().getAsyncThreshold();
        try (Scope scope = serviceSpan.makeCurrent()){
            if (configuration.getDelayMilliseconds() != null) {
                if (configuration.getDelayMilliseconds() > asyncTimeout/2) {
                    throw new RuntimeException(String.format("The delay between consecutive retries must be lower than: %s ms", asyncTimeout/2));
                }
                try {
                    Thread.sleep(configuration.getDelayMilliseconds());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            response = this.getRestTemplate(configuration)
                    .exchange(this.prepareUri(configuration), configuration.getHttpMethod(), this.buildHttpEntity(configuration), String.class);
            serviceSpan.setAttribute("response", response.toString());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            getLogger().error("Exception in HTTP request: ", e);
            response = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            getLogger().error("Exception in HTTP request: ", e);
            response = new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            serviceSpan.end();
        }

        VariableMap output = Variables.createVariables();
        if (response.getBody() == null) {
            response = new ResponseEntity<>(new JsonObject().toString(), response.getStatusCode());
        }
        output.putValue(HttpVariablesEnum.RESPONSE.getValue(), response.getBody());
        output.putValue(HttpVariablesEnum.STATUS_CODE.getValue(), response.getStatusCode().value());
        SpinJsonNode headersJsonNode = JSON(response.getHeaders());
        output.putValue(HttpVariablesEnum.RESPONSE_HEADERS.getValue(), headersJsonNode.toString());
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

        SpanBuilder spanBuilder = getTracer().spanBuilder("MIL-Adapter-RestCall-Execution" + new Date());
        if (parentSpanContext.isNotNull()) {
            SpanContext parentContext = SpanContext.createFromRemoteParent(parentSpanContext.getTraceId(), parentSpanContext.getSpanId(), TraceFlags.getSampled(), TraceState.getDefault());
            Span parentSpan = Span.wrap(parentContext);
            return spanBuilder.setParent(Context.current().with(parentSpan));
        }
        return spanBuilder;
    }
}
