package it.gov.pagopa.miladapter.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.model.ParentSpanContext;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;


public interface GenericRestExternalService  {



    URI prepareUri(Configuration configuration, String flow);

    Logger getLogger();

    Tracer getTracer();

    RestConfigurationProperties getRestConfigurationProperties();


    <T> HttpEntity<T> buildHttpEntity(Configuration configuration);


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

