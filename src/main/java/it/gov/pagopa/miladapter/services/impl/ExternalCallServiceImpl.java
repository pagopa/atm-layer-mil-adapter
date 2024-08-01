package it.gov.pagopa.miladapter.services.impl;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import it.gov.pagopa.miladapter.enums.FlowValues;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.ExternalCallService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.sonarsource.scanner.api.internal.shaded.minimaljson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;


@Slf4j
@Service
public class ExternalCallServiceImpl extends GenericRestExternalServiceAbstract implements ExternalCallService {

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    @Autowired
    RestTemplate restTemplate;


    @Override
    public URI prepareUri(Configuration configuration, String flow) {
        if (flow.equals(FlowValues.MIL.getValue())) {
            return HttpRequestUtils.buildURI(restConfigurationProperties.getMilBasePath(), configuration.getEndpoint(), configuration.getPathParams());
        } else if (flow.equals(FlowValues.IDPAY.getValue())) {
            return HttpRequestUtils.buildURI(restConfigurationProperties.getIdPayBasePath(), configuration.getEndpoint(), configuration.getPathParams());
        } else if (flow.equals(FlowValues.AUTH.getValue())) {
            log.info("--TEMPORARY-- Preparing URI for flow {}", flow);
            log.info("--TEMPORARY-- Mil Base path: {} , auth endpoint: {}", restConfigurationProperties.getMilBasePath(), restConfigurationProperties.getGetTokenEndpoint());
            return HttpRequestUtils.buildURI(restConfigurationProperties.getMilBasePath(), restConfigurationProperties.getGetTokenEndpoint());
        } else {
            throw new RuntimeException("Unrecognised flow: " + flow);
        }
    }



    @Override
    public SpanBuilder spanBuilder(Configuration configuration) {

        return super.spanBuilder(configuration);
    }



    public ResponseEntity executeExternalCall(Map<String, Object> body) {
        String flow = body.get(RequiredProcessVariables.FLOW.getEngineValue()).toString();
        Configuration configuration;
        if (flow.equals(FlowValues.AUTH.getValue())){
            configuration = EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationGenerateTokenCall(body);
        } else {
            configuration = EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCall(body, flow.equals(FlowValues.MIL.getValue()), flow.equals(FlowValues.IDPAY.getValue()));
        }
        ResponseEntity<String> response;

        SpanBuilder spanBuilder = this.spanBuilder(configuration);
        Span serviceSpan = spanBuilder.startSpan();
            try (Scope scope = serviceSpan.makeCurrent()) {
                URI url = this.prepareUri(configuration, flow);
                HttpEntity<String> entity = HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
                serviceSpan.setAttribute(SemanticAttributes.HTTP_METHOD, configuration.getHttpMethod().name());
                serviceSpan.setAttribute(SemanticAttributes.HTTP_URL, url.toString());
                if (entity.hasBody()) {
                    serviceSpan.setAttribute("http.body", entity.getBody());
                }
                serviceSpan.setAttribute("http.headers", entity.getHeaders().toString());
                serviceSpan.setAttribute("MIL.call.start.time", (LocalDateTime.now()).toString());
                response = restTemplate.exchange(url, configuration.getHttpMethod(), entity, String.class);
                serviceSpan.setAttribute("MIL.call.end.time", (LocalDateTime.now()).toString());
                if (response.getBody() == null) {
                    response = new ResponseEntity<>(new JsonObject().toString(), response.getStatusCode());
                }
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                getLogger().error("Exception in HTTP request: {}", e);
                response = new ResponseEntity<>(new JsonObject().toString(), e.getStatusCode());
                serviceSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, e.getStatusCode().value());
                serviceSpan.setAttribute("http.response.body", e.getResponseBodyAsString());
            } catch (ResourceAccessException e) {
                getLogger().error("Exception in HTTP request: {}", e);
                response = new ResponseEntity<>(new JsonObject().toString(), HttpStatus.GATEWAY_TIMEOUT);
            } catch (Exception e) {
                getLogger().error("Exception in HTTP request: {}", e);
                response = new ResponseEntity<>(new JsonObject().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            serviceSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, response.getStatusCode().value());
            serviceSpan.setAttribute("http.response.body", response.getBody());
            serviceSpan.setAttribute("http.response.headers", response.getHeaders().toString());
            serviceSpan.end();
        return response;
    }



    public HttpEntity<String> buildHttpEntity(Configuration configuration) {
        return HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
    }





}
