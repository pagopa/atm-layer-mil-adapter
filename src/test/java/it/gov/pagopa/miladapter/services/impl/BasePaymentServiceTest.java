package it.gov.pagopa.miladapter.services.impl;

import static it.gov.pagopa.miladapter.util.PaymentTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.gov.pagopa.miladapter.client.FeeRestClient;
import it.gov.pagopa.miladapter.client.NodeForPspWrapper;
import it.gov.pagopa.miladapter.client.model.GecGetFeesRequest;
import it.gov.pagopa.miladapter.client.model.GecGetFeesResponse;
import it.gov.pagopa.miladapter.client.model.BundleOption;
import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.properties.NodeErrorMappingProperties;
import it.gov.pagopa.miladapter.services.model.CommonHeader;
import it.gov.pagopa.miladapter.services.model.GetFeeResponse;
import it.gov.pagopa.miladapter.util.FeeCalculatorErrorCode;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class BasePaymentServiceTest {

  @Mock private NodeErrorMappingProperties nodeErrorMappingProperties;

  @Mock private NodeForPspWrapper nodeWrapper;

  @Mock private FeeRestClient feeRestClient;

  @InjectMocks private BasePaymentService basePaymentService;

  private static final String REQUEST_ID = "test-request-id";

  private CommonHeader commonHeader;
  private PspConfiguration pspConfiguration;

  @BeforeEach
  void setup() {
    // Common header
    commonHeader = new CommonHeader();
    commonHeader.setAcquirerId(ACQUIRER_ID);
    commonHeader.setTerminalId(TERMINAL_ID);
    commonHeader.setChannel(CHANNEL);
    commonHeader.setMerchantId("MERCHANT123");

    // PSP Configuration
    pspConfiguration = new PspConfiguration();
    pspConfiguration.setPsp("PSP_CODE");
    pspConfiguration.setBroker("BROKER_CODE");
    pspConfiguration.setChannel("CHANNEL_CODE");
    pspConfiguration.setPassword("PASSWORD");
  }

  // ==================== verifyPaymentNotice Tests ====================

  @Test
  void testVerifyPaymentNotice_Success() {
    VerifyPaymentNoticeReq request = new VerifyPaymentNoticeReq();
    VerifyPaymentNoticeRes expectedResponse = new VerifyPaymentNoticeRes();
    expectedResponse.setOutcome(StOutcome.OK);

    when(nodeWrapper.verifyPaymentNotice(request)).thenReturn(expectedResponse);

    VerifyPaymentNoticeRes result = basePaymentService.verifyPaymentNotice(request);

    assertNotNull(result);
    assertEquals(expectedResponse, result);
    assertEquals(StOutcome.OK, result.getOutcome());
    verify(nodeWrapper).verifyPaymentNotice(request);
  }

  @Test
  void testVerifyPaymentNotice_Error() {

    VerifyPaymentNoticeReq request = new VerifyPaymentNoticeReq();
    RuntimeException exception = new RuntimeException("Node error");

    when(nodeWrapper.verifyPaymentNotice(request)).thenThrow(exception);

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class, () -> basePaymentService.verifyPaymentNotice(request));

    assertEquals("Node error", thrown.getMessage());
    verify(nodeWrapper).verifyPaymentNotice(request);
  }

  // ==================== activatePaymentNoticeV2 Tests ====================

  @Test
  void testActivatePaymentNoticeV2_Success() {

    ActivatePaymentNoticeV2Request request = new ActivatePaymentNoticeV2Request();
    ActivatePaymentNoticeV2Response expectedResponse = new ActivatePaymentNoticeV2Response();
    expectedResponse.setOutcome(StOutcome.OK);

    when(nodeWrapper.activatePaymentNoticeV2(request)).thenReturn(expectedResponse);

    ActivatePaymentNoticeV2Response result =
        basePaymentService.activatePaymentNoticeV2(request);

    assertNotNull(result);
    assertEquals(expectedResponse, result);
    assertEquals(StOutcome.OK, result.getOutcome());
    verify(nodeWrapper).activatePaymentNoticeV2(request);
  }

  @Test
  void testActivatePaymentNoticeV2_Error() {

    ActivatePaymentNoticeV2Request request = new ActivatePaymentNoticeV2Request();
    RuntimeException exception = new RuntimeException("Activation error");

    when(nodeWrapper.activatePaymentNoticeV2(request)).thenThrow(exception);

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> basePaymentService.activatePaymentNoticeV2(request));

    assertEquals("Activation error", thrown.getMessage());
    verify(nodeWrapper).activatePaymentNoticeV2(request);
  }

  // ==================== sendPaymentOutcomeV2 Tests ====================

  @Test
  void testSendPaymentOutcomeV2_Success() {

    SendPaymentOutcomeV2Request request = new SendPaymentOutcomeV2Request();
    SendPaymentOutcomeV2Response expectedResponse = new SendPaymentOutcomeV2Response();
    expectedResponse.setOutcome(StOutcome.OK);

    when(nodeWrapper.sendPaymentOutcomeV2(request)).thenReturn(expectedResponse);

    SendPaymentOutcomeV2Response result = basePaymentService.sendPaymentOutcomeV2(request);

    assertNotNull(result);
    assertEquals(expectedResponse, result);
    assertEquals(StOutcome.OK, result.getOutcome());
    verify(nodeWrapper).sendPaymentOutcomeV2(request);
  }

  @Test
  void testSendPaymentOutcomeV2_Error() {

    SendPaymentOutcomeV2Request request = new SendPaymentOutcomeV2Request();
    RuntimeException exception = new RuntimeException("Send outcome error");

    when(nodeWrapper.sendPaymentOutcomeV2(request)).thenThrow(exception);

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class, () -> basePaymentService.sendPaymentOutcomeV2(request));

    assertEquals("Send outcome error", thrown.getMessage());
    verify(nodeWrapper).sendPaymentOutcomeV2(request);
  }

  // ==================== getFees Tests ====================

  @Test
  void testGetFees_Success() {

    GecGetFeesRequest request = new GecGetFeesRequest();
    GecGetFeesResponse gecResponse = new GecGetFeesResponse();
    BundleOption bundleOption = new BundleOption();
    bundleOption.setTaxPayerFee(100L);
    gecResponse.setBundleOptions(List.of(bundleOption));

    when(feeRestClient.getFees(REQUEST_ID, request)).thenReturn(Mono.just(gecResponse));

    GetFeeResponse response = basePaymentService.getFees(REQUEST_ID, request);

    assertNotNull(response);
    assertEquals(100L, response.getFee());
    verify(feeRestClient).getFees(REQUEST_ID, request);
  }

  @Test
  void testGetFees_NoFeeFound() {

    GecGetFeesRequest request = new GecGetFeesRequest();
    GecGetFeesResponse gecResponse = new GecGetFeesResponse();
    gecResponse.setBundleOptions(new ArrayList<>());

    when(feeRestClient.getFees(REQUEST_ID, request)).thenReturn(Mono.just(gecResponse));

    ResponseStatusException thrown =
        assertThrows(
            ResponseStatusException.class,
            () -> basePaymentService.getFees(REQUEST_ID, request));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    assertNotNull(thrown.getReason());
    assertTrue(thrown.getReason().contains(FeeCalculatorErrorCode.NO_FEE_FOUND));
    verify(feeRestClient).getFees(REQUEST_ID, request);
  }

  @Test
  void testGetFees_ClientError() {

    GecGetFeesRequest request = new GecGetFeesRequest();
    RuntimeException exception = new RuntimeException("GEC error");

    when(feeRestClient.getFees(REQUEST_ID, request)).thenReturn(Mono.error(exception));

    ResponseStatusException thrown =
        assertThrows(
            ResponseStatusException.class,
            () -> basePaymentService.getFees(REQUEST_ID, request));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    assertNotNull(thrown.getReason());
    assertTrue(thrown.getReason().contains(FeeCalculatorErrorCode.ERROR_RETRIEVING_FEES));
    verify(feeRestClient).getFees(REQUEST_ID, request);
  }

  // ==================== remapNodeFaultToOutcome Tests ====================

  @Test
  void testRemapNodeFaultToOutcome_WithBothCodes() {

    Map<String, Integer> errorMap = new HashMap<>();
    errorMap.put("FAULT_CODE-ORIGINAL_CODE", 1);
    List<String> outcomes = List.of("UNEXPECTED_ERROR", "SPECIFIC_ERROR");

    when(nodeErrorMappingProperties.getMap()).thenReturn(errorMap);
    when(nodeErrorMappingProperties.getOutcomes()).thenReturn(outcomes);

    String result = basePaymentService.remapNodeFaultToOutcome("FAULT_CODE", "ORIGINAL_CODE");

    assertEquals("SPECIFIC_ERROR", result);
  }

  @Test
  void testRemapNodeFaultToOutcome_WithFaultCodeOnly() {

    Map<String, Integer> errorMap = new HashMap<>();
    errorMap.put("FAULT_CODE", 1);
    List<String> outcomes = List.of("UNEXPECTED_ERROR", "SPECIFIC_ERROR");

    when(nodeErrorMappingProperties.getMap()).thenReturn(errorMap);
    when(nodeErrorMappingProperties.getOutcomes()).thenReturn(outcomes);

    String result = basePaymentService.remapNodeFaultToOutcome("FAULT_CODE", null);

    assertEquals("SPECIFIC_ERROR", result);
  }

  @Test
  void testRemapNodeFaultToOutcome_NoMappingFound() {

    Map<String, Integer> errorMap = new HashMap<>();
    List<String> outcomes = List.of("UNEXPECTED_ERROR", "SPECIFIC_ERROR");

    when(nodeErrorMappingProperties.getMap()).thenReturn(errorMap);
    when(nodeErrorMappingProperties.getOutcomes()).thenReturn(outcomes);

    String result = basePaymentService.remapNodeFaultToOutcome("UNKNOWN_CODE", "UNKNOWN_ORIGINAL");

    assertEquals("UNEXPECTED_ERROR", result);
  }

  @Test
  void testRemapNodeFaultToOutcome_EmptyOriginalCode() {

    Map<String, Integer> errorMap = new HashMap<>();
    errorMap.put("FAULT_CODE", 2);
    List<String> outcomes = List.of("UNEXPECTED_ERROR", "ERROR_1", "ERROR_2");

    when(nodeErrorMappingProperties.getMap()).thenReturn(errorMap);
    when(nodeErrorMappingProperties.getOutcomes()).thenReturn(outcomes);

    String result = basePaymentService.remapNodeFaultToOutcome("FAULT_CODE", "");

    assertEquals("ERROR_2", result);
  }

  // ==================== getDeviceId Tests ====================

  @Test
  void testGetDeviceId_Success() {

    String deviceId = basePaymentService.getDeviceId(commonHeader);

    assertNotNull(deviceId);
    assertEquals(ACQUIRER_ID + "|" + TERMINAL_ID, deviceId);
  }

  @Test
  void testGetDeviceId_WithDifferentValues() {

    commonHeader.setAcquirerId("ACQUIRER_ABC");
    commonHeader.setTerminalId("TERMINAL_XYZ");

    String deviceId = basePaymentService.getDeviceId(commonHeader);

    assertEquals("ACQUIRER_ABC|TERMINAL_XYZ", deviceId);
  }

  // ==================== getTimestamp Tests ====================

  @Test
  void testGetTimestamp_FormatCheck() {

    String timestamp = BasePaymentService.getTimestamp();

    assertNotNull(timestamp);
    assertDoesNotThrow(() -> LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  }

  @Test
  void testGetTimestamp_NoMilliseconds() {

    String timestamp = BasePaymentService.getTimestamp();

    assertFalse(timestamp.contains("."), "Timestamp should not contain milliseconds");
  }

  @Test
  void testGetTimestamp_ConsistentFormat() {

    String timestamp1 = BasePaymentService.getTimestamp();
    String timestamp2 = BasePaymentService.getTimestamp();

    assertEquals(timestamp1.length(), timestamp2.length());
    assertTrue(timestamp1.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"));
    assertTrue(timestamp2.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"));
  }
}
