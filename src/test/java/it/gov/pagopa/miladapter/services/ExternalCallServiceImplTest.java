package it.gov.pagopa.miladapter.services;

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
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.impl.*;
import it.gov.pagopa.miladapter.services.model.ActivatePaymentNoticeResponse;
import it.gov.pagopa.miladapter.services.model.GetFeeResponse;
import it.gov.pagopa.miladapter.services.model.VerifyPaymentNoticeResponse;
import it.gov.pagopa.miladapter.util.PaymentTestData;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class ExternalCallServiceImplTest {

  private ExternalCallServiceNewImpl spyExternalCallService;
  private RestTemplate restTemplate;
  private RestConfigurationProperties restConfigurationProperties;
  private Map<String, Object> testVariables;
  private VerifyPaymentNoticeService verifyPaymentNoticeService;
  private ActivatePaymentNoticeService activatePaymentNoticeService;
  private FeeCalculatorService feeCalculatorService;
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
    feeCalculatorService = mock(FeeCalculatorService.class);
    paymentService = mock(PaymentService.class);
    objectMapper = new ObjectMapper();

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
    spyExternalCallService =
        new ExternalCallServiceNewImpl(
            restConfigurationProperties,
            restTemplate,
            objectMapper,
            verifyPaymentNoticeService,
            activatePaymentNoticeService,
            feeCalculatorService,
            paymentService);

    // Set the tracer using reflection
    setPrivateField(spyExternalCallService, "tracer", tracer);

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
    mockResponse.setAmount(new java.math.BigInteger("1000"));
    when(verifyPaymentNoticeService.verifyByQrCode(any(), eq(QRCODE)))
        .thenReturn(ResponseEntity.ok(mockResponse));

    ResponseEntity<String> result = spyExternalCallService.executeExternalCall(testVariables);

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
    mockResponse.setAmount(new java.math.BigInteger("1000"));
    when(verifyPaymentNoticeService.verifyByTaxCodeAndNoticeNumber(
            any(), eq(PA_TAX_CODE), eq(NOTICE_NUMBER)))
        .thenReturn(ResponseEntity.ok(mockResponse));
    
    ResponseEntity<String> result = spyExternalCallService.executeExternalCall(testVariables);

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
    
    ResponseEntity<String> result = spyExternalCallService.executeExternalCall(testVariables);
    
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
    mockResponse.setAmount(new java.math.BigInteger("1000"));
    when(activatePaymentNoticeService.activateByTaxCodeAndNoticeNumber(
            any(), eq(PA_TAX_CODE), eq(NOTICE_NUMBER), any()))
        .thenReturn(ResponseEntity.ok(mockResponse));
    
    ResponseEntity<String> result = spyExternalCallService.executeExternalCall(testVariables);

    verify(activatePaymentNoticeService)
        .activateByTaxCodeAndNoticeNumber(any(), eq(PA_TAX_CODE), eq(NOTICE_NUMBER), any());
    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(restTemplate, never())
        .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
  }

  @Test
  void executeExternalCall_getFee() throws JsonProcessingException {
    // Prepare test variables for MIL flow with local endpoint
    testVariables.put(RequiredProcessVariables.FLOW.getEngineValue(), FlowValues.MIL.getValue());
    testVariables.put("url", "/mil-fee-calculator/fees");
    testVariables.put("method", "POST");
    testVariables.put("body", objectMapper.writeValueAsString(PaymentTestData.getFeeRequest()));
    
    GetFeeResponse mockResponse = new GetFeeResponse();
    mockResponse.setFee(1000L);
    when(feeCalculatorService.getFee(any(), any())).thenReturn(ResponseEntity.ok(mockResponse));

    ResponseEntity<String> result = spyExternalCallService.executeExternalCall(testVariables);

    verify(feeCalculatorService).getFee(any(), any());
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

      ResponseEntity<String> result = spyExternalCallService.executeExternalCall(testVariables);
      
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
    ExternalCallServiceNewImpl spyService =
        Mockito.spy(
            new ExternalCallServiceNewImpl(
                restConfigurationProperties,
                restTemplate,
                objectMapper,
                verifyPaymentNoticeService,
                activatePaymentNoticeService,
                feeCalculatorService,
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
    ExternalCallServiceNewImpl spyService =
        Mockito.spy(
            new ExternalCallServiceNewImpl(
                restConfigurationProperties,
                restTemplate,
                objectMapper,
                verifyPaymentNoticeService,
                activatePaymentNoticeService,
                feeCalculatorService,
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
    ExternalCallServiceNewImpl spyService =
        Mockito.spy(
            new ExternalCallServiceNewImpl(
                restConfigurationProperties,
                restTemplate,
                objectMapper,
                verifyPaymentNoticeService,
                activatePaymentNoticeService,
                feeCalculatorService,
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
