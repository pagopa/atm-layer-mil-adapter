package it.gov.pagopa.miladapter.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import it.gov.pagopa.miladapter.enums.FlowValues;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.AuthProperties;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.model.ActivatePaymentNoticeResponse;
import it.gov.pagopa.miladapter.services.model.VerifyPaymentNoticeResponse;
import it.gov.pagopa.miladapter.util.PaymentTestData;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

class ExternalCallServiceImplTest {

  private ExternalCallServiceImpl spyExternalCallServiceImpl;
  private RestTemplate restTemplate;
  private RestConfigurationProperties restConfigurationProperties;
  private Map<String, Object> testVariables;
  private VerifyPaymentNoticeService verifyPaymentNoticeService;
  private ActivatePaymentNoticeService activatePaymentNoticeService;
  private PaymentService paymentService;
  private ObjectMapper objectMapper;
  private Tracer tracer;
  private final String QRCODE = "UEFHT1BBfDAwMnwwMDAwMDAwMDAwMDAwMDAwMDB8Nzc3Nzc3Nzc3Nzd8OTk5OQ";
  private final String PA_TAX_CODE = "20000000000";
  private final String NOTICE_NUMBER = "302051234567890125";

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    restTemplate = mock(RestTemplate.class);
    restConfigurationProperties = mock(RestConfigurationProperties.class);
    verifyPaymentNoticeService = mock(VerifyPaymentNoticeService.class);
    activatePaymentNoticeService = mock(ActivatePaymentNoticeService.class);
    paymentService = mock(PaymentService.class);
    objectMapper = new ObjectMapper();
    AuthProperties authProperties = mock(AuthProperties.class);

    // Mock OpenTelemetry components
    tracer = mock(Tracer.class);
    SpanBuilder spanBuilder = mock(SpanBuilder.class);
    Span span = mock(Span.class);
    Scope scope = mock(Scope.class);

    when(tracer.spanBuilder(any())).thenReturn(spanBuilder);
    when(spanBuilder.startSpan()).thenReturn(span);
    when(span.makeCurrent()).thenReturn(scope);
    when(span.setAttribute(any(String.class), any())).thenReturn(span);
    when(span.setAttribute(any(String.class), anyLong())).thenReturn(span);

    // Use real implementation and inject mocked dependencies
    spyExternalCallServiceImpl =
        new ExternalCallServiceImpl(
            restConfigurationProperties,
            restTemplate,
            objectMapper,
            verifyPaymentNoticeService,
            activatePaymentNoticeService,
            paymentService);

    // Set the tracer using reflection
    setPrivateField(spyExternalCallServiceImpl, "tracer", tracer);

    when(restConfigurationProperties.getMilBasePath()).thenReturn("http://mil-base-path");
    when(restConfigurationProperties.getIdPayBasePath()).thenReturn("http://idpay-base-path");
    when(restConfigurationProperties.getGetTokenEndpoint()).thenReturn("/auth/token");
    when(restConfigurationProperties.getAuth()).thenReturn(authProperties);
    when(authProperties.getClientId()).thenReturn("test-client-id");
    when(authProperties.getClientSecret()).thenReturn("test-client-secret");
    when(authProperties.getGrantType()).thenReturn("client_credentials");

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
    testVariables.put("activityParentSpan", null);
    testVariables.put("transactionId", "transactionId");
  }

  @Test
  void executeExternalCall_VerifyByQrCode_Get() throws JsonProcessingException {
    // Prepare test variables for MIL flow with local endpoint
    testVariables.put(RequiredProcessVariables.FLOW.getEngineValue(), FlowValues.MIL.getValue());
    testVariables.put("url", "/mil-payment-notice/paymentNotices/{qrCode}");
    testVariables.put("method", "GET");
    testVariables.put("body", "");
    testVariables.put("PathParams", Map.of("qrCode", QRCODE));

    VerifyPaymentNoticeResponse mockResponse = new VerifyPaymentNoticeResponse();
    mockResponse.setOutcome("OK");
    mockResponse.setAmount(new BigDecimal("1000"));
    when(verifyPaymentNoticeService.verifyByQrCode(any(), eq(QRCODE)))
        .thenReturn(ResponseEntity.ok(mockResponse));

    ResponseEntity<String> result = spyExternalCallServiceImpl.executeExternalCall(testVariables);

    verify(verifyPaymentNoticeService).verifyByQrCode(any(), eq(QRCODE));
    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(restTemplate, never())
        .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
  }

  @Test
  void executeExternalCall_VerifyByTaxCodeAndNoticeNumber_Get() throws JsonProcessingException {
    // Prepare test variables for MIL flow with local endpoint
    testVariables.put(RequiredProcessVariables.FLOW.getEngineValue(), FlowValues.MIL.getValue());
    testVariables.put("url", "/mil-payment-notice/paymentNotices/{paTaxCode}/{noticeNumber}");
    testVariables.put("method", "GET");
    testVariables.put("body", "");
    testVariables.put(
        "PathParams", Map.of("paTaxCode", PA_TAX_CODE, "noticeNumber", NOTICE_NUMBER));

    VerifyPaymentNoticeResponse mockResponse = new VerifyPaymentNoticeResponse();
    mockResponse.setOutcome("OK");
    mockResponse.setAmount(new BigDecimal("1000"));
    when(verifyPaymentNoticeService.verifyByTaxCodeAndNoticeNumber(
            any(), eq(PA_TAX_CODE), eq(NOTICE_NUMBER)))
        .thenReturn(ResponseEntity.ok(mockResponse));
    
    ResponseEntity<String> result = spyExternalCallServiceImpl.executeExternalCall(testVariables);

    verify(verifyPaymentNoticeService)
        .verifyByTaxCodeAndNoticeNumber(any(), eq(PA_TAX_CODE), eq(NOTICE_NUMBER));
    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(restTemplate, never())
        .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
  }

  @Test
  void executeExternalCall_ActivateByQrCode_Patch() throws JsonProcessingException {
    // Prepare test variables for MIL flow with activate endpoint
    testVariables.put(RequiredProcessVariables.FLOW.getEngineValue(), FlowValues.MIL.getValue());
    testVariables.put("url", "/mil-payment-notice/paymentNotices/{qrCode}");
    testVariables.put("method", "PATCH");
    testVariables.put("PathParams", Map.of("qrCode", QRCODE));
    testVariables.put("body", objectMapper.writeValueAsString(PaymentTestData.getActivatePaymentRequest()));

    
    ActivatePaymentNoticeResponse mockResponse = new ActivatePaymentNoticeResponse();
    mockResponse.setOutcome("OK");
    mockResponse.setPaymentToken("token_123");
    when(activatePaymentNoticeService.activateByQrCode(any(), eq(QRCODE), any()))
        .thenReturn(ResponseEntity.ok(mockResponse));
    
    ResponseEntity<String> result = spyExternalCallServiceImpl.executeExternalCall(testVariables);
    
    verify(activatePaymentNoticeService).activateByQrCode(any(), eq(QRCODE), any());
    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(restTemplate, never())
        .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
  }

  @Test
  void executeExternalCall_ActivateByTaxCodeAndNoticeNumber_Patch() throws JsonProcessingException {
    // Prepare test variables for MIL flow with local endpoint
    testVariables.put(RequiredProcessVariables.FLOW.getEngineValue(), FlowValues.MIL.getValue());
    testVariables.put("url", "/mil-payment-notice/paymentNotices/{paTaxCode}/{noticeNumber}");
    testVariables.put("method", "PATCH");
    testVariables.put("body", objectMapper.writeValueAsString(PaymentTestData.getActivatePaymentRequest()));
    testVariables.put(
        "PathParams", Map.of("paTaxCode", PA_TAX_CODE, "noticeNumber", NOTICE_NUMBER));
    
    ActivatePaymentNoticeResponse mockResponse = new ActivatePaymentNoticeResponse();
    mockResponse.setOutcome("OK");
    mockResponse.setAmount(new BigDecimal("1000"));
    when(activatePaymentNoticeService.activateByTaxCodeAndNoticeNumber(
            any(), eq(PA_TAX_CODE), eq(NOTICE_NUMBER), any()))
        .thenReturn(ResponseEntity.ok(mockResponse));
    
    ResponseEntity<String> result = spyExternalCallServiceImpl.executeExternalCall(testVariables);

    verify(activatePaymentNoticeService)
        .activateByTaxCodeAndNoticeNumber(any(), eq(PA_TAX_CODE), eq(NOTICE_NUMBER), any());
    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(restTemplate, never())
        .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
  }

  @Test
  void executeExternalCall_sendPaymentOutcome() throws JsonProcessingException {
      // Prepare test variables for MIL flow with local endpoint
      testVariables.put(RequiredProcessVariables.FLOW.getEngineValue(), FlowValues.MIL.getValue());
      testVariables.put("url", "/mil-payment-notice/payments/{transactionId}/sendPaymentOutcome");
      testVariables.put("method", "PATCH");
      testVariables.put("body", objectMapper.writeValueAsString(PaymentTestData.getClosePaymentRequest(true)));
      
      when(paymentService.sendPaymentOutcome(any(), any(), any())).thenReturn(ResponseEntity.accepted().build());

      ResponseEntity<String> result = spyExternalCallServiceImpl.executeExternalCall(testVariables);
      
      verify(paymentService).sendPaymentOutcome(any(), any(), any());
      assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
      verify(restTemplate, never())
              .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
  }

  @Test
  void executeExternalCallTestClientException()
      throws URISyntaxException,
          JsonProcessingException,
          NoSuchFieldException,
          IllegalAccessException {
    // Create spy only for this test where we need to mock prepareUri
    ExternalCallServiceImpl spyService =
        Mockito.spy(
            new ExternalCallServiceImpl(
                restConfigurationProperties,
                restTemplate,
                objectMapper,
                verifyPaymentNoticeService,
                activatePaymentNoticeService,
                paymentService));

    // Set the tracer for this spy as well
    setPrivateField(spyService, "tracer", tracer);

    doReturn(new URI("http://mil-base-path/endpoint/params"))
        .when(spyService)
        .prepareUri(any(), any());

    HttpClientErrorException e = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "status");
    when(restTemplate.exchange(
            any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
        .thenThrow(e);

    ResponseEntity<String> result = spyService.executeExternalCall(testVariables);

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  void executeExternalCallTestOK()
      throws URISyntaxException,
          JsonProcessingException,
          NoSuchFieldException,
          IllegalAccessException {
    // Create spy only for this test where we need to mock prepareUri
    ExternalCallServiceImpl spyService =
        Mockito.spy(
            new ExternalCallServiceImpl(
                restConfigurationProperties,
                restTemplate,
                objectMapper,
                verifyPaymentNoticeService,
                activatePaymentNoticeService,
                paymentService));

    // Set the tracer for this spy as well
    setPrivateField(spyService, "tracer", tracer);

    doReturn(new URI("http://mil-base-path/endpoint/params"))
        .when(spyService)
        .prepareUri(any(), any());

    ResponseEntity<String> responseEntity = new ResponseEntity<>("body", HttpStatus.OK);
    when(restTemplate.exchange(
            any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
        .thenReturn(responseEntity);

    ResponseEntity<String> result = spyService.executeExternalCall(testVariables);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals("body", result.getBody());
  }

  @Test
  void executeExternalCallTestGenericException()
      throws URISyntaxException,
          JsonProcessingException,
          NoSuchFieldException,
          IllegalAccessException {
    ExternalCallServiceImpl spyService =
        Mockito.spy(
            new ExternalCallServiceImpl(
                restConfigurationProperties,
                restTemplate,
                objectMapper,
                verifyPaymentNoticeService,
                activatePaymentNoticeService,
                paymentService));

    setPrivateField(spyService, "tracer", tracer);

    doReturn(new URI("http://mil-base-path/endpoint/params"))
        .when(spyService)
        .prepareUri(any(), any());

    RuntimeException e = new RuntimeException("test exception");
    when(restTemplate.exchange(
            any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
        .thenThrow(e);

    ResponseEntity<String> result = spyService.executeExternalCall(testVariables);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
  }

  private void setPrivateField(Object targetObject, String fieldName, Object fieldValue)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = targetObject.getClass().getSuperclass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(targetObject, fieldValue);
  }

  @Test
  void prepareUriTestOK() {
    Configuration configuration = new Configuration();
    configuration.setEndpoint("endpoint");
    configuration.setPathParams(new HashMap<>());

    URI resultMil = spyExternalCallServiceImpl.prepareUri(configuration, FlowValues.MIL.getValue());
    assertEquals("http://mil-base-pathendpoint", resultMil.toString());

    URI resultIdPay = spyExternalCallServiceImpl.prepareUri(configuration, FlowValues.IDPAY.getValue());
    assertEquals("http://idpay-base-pathendpoint", resultIdPay.toString());

    URI resultAuth = spyExternalCallServiceImpl.prepareUri(configuration, FlowValues.AUTH.getValue());
    assertEquals("http://mil-base-path/auth/token", resultAuth.toString());

    try {
      spyExternalCallServiceImpl.prepareUri(configuration, "unknown flow");
    } catch (Exception e) {
      assertEquals("Unrecognised flow: unknown flow", e.getMessage());
    }
  }

  @Test
  void executeExternalCall_AuthFlow_Success()
      throws URISyntaxException, JsonProcessingException, NoSuchFieldException,
          IllegalAccessException {
    // Prepare test variables for AUTH flow
    testVariables.put(RequiredProcessVariables.FLOW.getEngineValue(), FlowValues.AUTH.getValue());
    testVariables.put("url", "/auth/token");
    testVariables.put("method", "POST");
    testVariables.put("body", "{\"grant_type\":\"client_credentials\"}");

    // Create spy to mock prepareUri
    ExternalCallServiceImpl spyService =
        Mockito.spy(
            new ExternalCallServiceImpl(
                restConfigurationProperties,
                restTemplate,
                objectMapper,
                verifyPaymentNoticeService,
                activatePaymentNoticeService,
                paymentService));

    setPrivateField(spyService, "tracer", tracer);

    doReturn(new URI("http://mil-base-path/auth/token"))
        .when(spyService)
        .prepareUri(any(), eq(FlowValues.AUTH.getValue()));

    ResponseEntity<String> mockAuthResponse =
        new ResponseEntity<>("{\"access_token\":\"token123\"}", HttpStatus.OK);
    when(restTemplate.exchange(
            any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
        .thenReturn(mockAuthResponse);

    ResponseEntity<String> result = spyService.executeExternalCall(testVariables);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals("{\"access_token\":\"token123\"}", result.getBody());
    verify(restTemplate)
        .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
  }

  @Test
  void executeExternalCall_LocalMilCall_Exception() throws JsonProcessingException {
    // Prepare test variables for MIL flow with local endpoint that will throw exception
    testVariables.put(RequiredProcessVariables.FLOW.getEngineValue(), FlowValues.MIL.getValue());
    testVariables.put("url", "/mil-payment-notice/paymentNotices/{qrCode}");
    testVariables.put("method", "GET");
    testVariables.put("body", "");
    testVariables.put("PathParams", Map.of("qrCode", QRCODE));

    // Mock the service to throw an exception
    when(verifyPaymentNoticeService.verifyByQrCode(any(), eq(QRCODE)))
        .thenThrow(new RuntimeException("Service error"));

    ResponseEntity<String> result = spyExternalCallServiceImpl.executeExternalCall(testVariables);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    verify(verifyPaymentNoticeService).verifyByQrCode(any(), eq(QRCODE));
    verify(restTemplate, never())
        .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
  }

  @Test
  void executeExternalCall_LocalMilCall_UnsupportedEndpoint() throws JsonProcessingException {
    // Prepare test variables for MIL flow with unsupported local endpoint
    testVariables.put(RequiredProcessVariables.FLOW.getEngineValue(), FlowValues.MIL.getValue());
    testVariables.put("url", "/mil-payment-notice/paymentNotices/{paTaxCode}/{noticeNumber}");
    testVariables.put("method", "DELETE");
    testVariables.put("body", "");
    testVariables.put("PathParams", Map.of());

    ResponseEntity<String> result = spyExternalCallServiceImpl.executeExternalCall(testVariables);

    assertEquals(HttpStatus.NOT_IMPLEMENTED, result.getStatusCode());
    verify(verifyPaymentNoticeService, never()).verifyByQrCode(any(), any());
    verify(activatePaymentNoticeService, never()).activateByQrCode(any(), any(), any());
    verify(paymentService, never()).sendPaymentOutcome(any(), any(), any());
    verify(restTemplate, never())
        .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
  }

  @Test
  void executeExternalCall_HttpCall_NullResponseBody()
      throws URISyntaxException, JsonProcessingException, NoSuchFieldException,
          IllegalAccessException {
    // Create spy to mock prepareUri
    ExternalCallServiceImpl spyService =
        Mockito.spy(
            new ExternalCallServiceImpl(
                restConfigurationProperties,
                restTemplate,
                objectMapper,
                verifyPaymentNoticeService,
                activatePaymentNoticeService,
                paymentService));

    setPrivateField(spyService, "tracer", tracer);

    doReturn(new URI("http://mil-base-path/endpoint/params"))
        .when(spyService)
        .prepareUri(any(), any());

    // Mock response with null body
    ResponseEntity<String> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
    when(restTemplate.exchange(
            any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
        .thenReturn(responseEntity);

    ResponseEntity<String> result = spyService.executeExternalCall(testVariables);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals("{}", result.getBody()); // Should return empty JSON object
  }

  @Test
  void executeExternalCall_HttpCall_ResourceAccessException()
      throws URISyntaxException, JsonProcessingException, NoSuchFieldException,
          IllegalAccessException {
    // Create spy to mock prepareUri
    ExternalCallServiceImpl spyService =
        Mockito.spy(
            new ExternalCallServiceImpl(
                restConfigurationProperties,
                restTemplate,
                objectMapper,
                verifyPaymentNoticeService,
                activatePaymentNoticeService,
                paymentService));

    setPrivateField(spyService, "tracer", tracer);

    doReturn(new URI("http://mil-base-path/endpoint/params"))
        .when(spyService)
        .prepareUri(any(), any());

    // Mock ResourceAccessException (timeout or connection error)
    ResourceAccessException exception =
        new ResourceAccessException("Connection timeout");
    when(restTemplate.exchange(
            any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
        .thenThrow(exception);

    ResponseEntity<String> result = spyService.executeExternalCall(testVariables);

    assertEquals(HttpStatus.GATEWAY_TIMEOUT, result.getStatusCode());
    assertEquals("{}", result.getBody());
  }
}
