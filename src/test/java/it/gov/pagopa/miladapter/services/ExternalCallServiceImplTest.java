package it.gov.pagopa.miladapter.services;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.context.Scope;
import it.gov.pagopa.miladapter.services.impl.ExternalCallServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExternalCallServiceImplTest {
    ExternalCallServiceImpl spyExternalCallService = Mockito.spy(new ExternalCallServiceImpl());
    Map<String, Object> testVariables = new HashMap<>();

    @BeforeEach
    void setUp() throws URISyntaxException {
        testVariables.put("headers", new HashMap<>());
        testVariables.put("url", "url");
        testVariables.put("method", "GET");
        testVariables.put("millAccessToken", "millAccessToken");
        testVariables.put("body", "body");
        testVariables.put("PathParams", new HashMap<>());
        testVariables.put("activityParentSpan", "activityParentSpan");
        testVariables.put("transactionId", "transactionId");
        doReturn(new URI("http://mil-base-path/endpoint/params")).when(spyExternalCallService).prepareUri(any());
        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        Span span = mock(Span.class);
        when(spanBuilder.startSpan()).thenReturn(span);
        when(span.makeCurrent()).thenReturn(mock(Scope.class));
        when(span.setAttribute(any(String.class), any())).thenReturn(span);
        Mockito.doNothing().when(span).end();
        doReturn(spanBuilder).when(spyExternalCallService).spanBuilder(any());
    }

    @Test
    void executeExternalCallTestOK() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ResponseEntity responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn("body");
        when(responseEntity.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));
        when(responseEntity.getHeaders()).thenReturn(new HttpHeaders());
        when(responseEntity.getBody()).thenReturn("body");
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(responseEntity);
        doReturn(restTemplate).when(spyExternalCallService).getRestTemplate(any());
        ResponseEntity result = spyExternalCallService.executeExternalCall(testVariables);
        assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
        assertEquals("body", result.getBody());
    }

    @Test
    void executeExternalCallTestClientException() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        HttpClientErrorException e = new HttpClientErrorException("test exception", HttpStatusCode.valueOf(400), "status", null, null, null);
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenThrow(e);
        doReturn(restTemplate).when(spyExternalCallService).getRestTemplate(any());
        ResponseEntity result = spyExternalCallService.executeExternalCall(testVariables);
        assertEquals(HttpStatusCode.valueOf(400), result.getStatusCode());
    }

    @Test
    void executeExternalCallTestGenericException() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        RuntimeException e = new RuntimeException("test exception");
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenThrow(e);
        doReturn(restTemplate).when(spyExternalCallService).getRestTemplate(any());
        ResponseEntity result = spyExternalCallService.executeExternalCall(testVariables);
        assertEquals(HttpStatusCode.valueOf(500), result.getStatusCode());
    }
}
