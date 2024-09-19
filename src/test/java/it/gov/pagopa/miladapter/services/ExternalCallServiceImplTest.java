package it.gov.pagopa.miladapter.services;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.context.Scope;
import it.gov.pagopa.miladapter.enums.FlowValues;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.impl.ExternalCallServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ExternalCallServiceImplTest {

    private ExternalCallServiceImpl spyExternalCallService;
    private RestTemplate restTemplate;
    private RestConfigurationProperties restConfigurationProperties;
    private Map<String, Object> testVariables;

    @BeforeEach
    void setUp() throws URISyntaxException, NoSuchFieldException, IllegalAccessException {
        spyExternalCallService = Mockito.spy(new ExternalCallServiceImpl());
        restTemplate = mock(RestTemplate.class);
        restConfigurationProperties = mock(RestConfigurationProperties.class);

        setPrivateField(spyExternalCallService, "restTemplate", restTemplate);
        setPrivateField(spyExternalCallService, "restConfigurationProperties", restConfigurationProperties);

        when(restConfigurationProperties.getMilBasePath()).thenReturn("http://mil-base-path");
        when(restConfigurationProperties.getIdPayBasePath()).thenReturn("http://idpay-base-path");
        when(restConfigurationProperties.getGetTokenEndpoint()).thenReturn("/auth/token");

        testVariables = new HashMap<>();
        HashMap<String, Object> headersMap = new HashMap<>();
        headersMap.put("AcquirerId", "bank_id");
        headersMap.put("Channel", "ATM");
        headersMap.put("TerminalId", "term_id");
        testVariables.put("headers", headersMap);
        testVariables.put(RequiredProcessVariables.FLOW.getEngineValue(), FlowValues.MIL.getValue());
        testVariables.put("url", "url");
        testVariables.put("method", "GET");
        testVariables.put("millAccessToken", "millAccessToken");
        testVariables.put("body", "body");
        testVariables.put("PathParams", new HashMap<>());
        testVariables.put("activityParentSpan", "activityParentSpan");
        testVariables.put("transactionId", "transactionId");


        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        Span span = mock(Span.class);
        when(spanBuilder.startSpan()).thenReturn(span);
        when(span.makeCurrent()).thenReturn(mock(Scope.class));
        when(span.setAttribute(any(String.class), any())).thenReturn(span);
        Mockito.doNothing().when(span).end();
        doReturn(spanBuilder).when(spyExternalCallService).spanBuilder(any());
    }

    @Test
    void executeExternalCallTestOK() throws URISyntaxException {
        doReturn(new URI("http://mil-base-path/endpoint/params")).when(spyExternalCallService).prepareUri(any(), any());

        ResponseEntity<String> responseEntity = new ResponseEntity<>("body", HttpStatus.OK);
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);

        ResponseEntity<String> result = spyExternalCallService.executeExternalCall(testVariables);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("body", result.getBody());
    }

    @Test
    void executeExternalCallTestClientException() throws URISyntaxException {
        doReturn(new URI("http://mil-base-path/endpoint/params")).when(spyExternalCallService).prepareUri(any(), any());

        HttpClientErrorException e = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "status");
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenThrow(e);

        ResponseEntity<String> result = spyExternalCallService.executeExternalCall(testVariables);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void executeExternalCallTestGenericException() throws URISyntaxException {
        doReturn(new URI("http://mil-base-path/endpoint/params")).when(spyExternalCallService).prepareUri(any(), any());

        RuntimeException e = new RuntimeException("test exception");
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenThrow(e);

        ResponseEntity<String> result = spyExternalCallService.executeExternalCall(testVariables);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    private void setPrivateField(Object targetObject, String fieldName, Object fieldValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, fieldValue);
    }

    @Test
    void prepareUriTestOK(){
        Configuration configuration = new Configuration();
        configuration.setEndpoint("endpoint");
        configuration.setPathParams(new HashMap<>());

        URI resultMil = spyExternalCallService.prepareUri(configuration, FlowValues.MIL.getValue());
        assertEquals("http://mil-base-pathendpoint", resultMil.toString());

        URI resultIdPay = spyExternalCallService.prepareUri(configuration, FlowValues.IDPAY.getValue());
        assertEquals("http://idpay-base-pathendpoint", resultIdPay.toString());

        URI resultAuth = spyExternalCallService.prepareUri(configuration, FlowValues.AUTH.getValue());
        assertEquals("http://mil-base-path/auth/token", resultAuth.toString());

        try {
            spyExternalCallService.prepareUri(configuration, "unknown flow");
        } catch (Exception e) {
            assertEquals("Unrecognised flow: unknown flow", e.getMessage());
        }

    }


}
