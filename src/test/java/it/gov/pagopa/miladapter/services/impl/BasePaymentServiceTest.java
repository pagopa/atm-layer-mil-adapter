package it.gov.pagopa.miladapter.services.impl;

import static it.gov.pagopa.miladapter.util.PaymentTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.gov.pagopa.miladapter.client.NodeForPspWrapper;
import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.properties.NodeErrorMappingProperties;
import it.gov.pagopa.miladapter.services.model.CommonHeader;
import it.gov.pagopa.miladapter.services.model.Fault;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasePaymentServiceTest {

  @Mock private NodeErrorMappingProperties nodeErrorMappingProperties;

  @Mock private NodeForPspWrapper nodeWrapper;

  @InjectMocks private BasePaymentService basePaymentService;

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

  // ==================== setFaultDetails Tests ====================

  @Test
  void testSetFaultDetails_AllFieldsPopulated() {
    CtFaultBean ctFaultBean = new CtFaultBean();
    ctFaultBean.setId("FAULT_ID_123");
    ctFaultBean.setFaultCode("PAA_PAGAMENTO_DUPLICATO");
    ctFaultBean.setFaultString("Payment duplicate fault");
    ctFaultBean.setDescription("Il pagamento è già stato processato");
    ctFaultBean.setSerial(12345);
    ctFaultBean.setOriginalFaultCode("ORIGINAL_CODE_123");
    ctFaultBean.setOriginalFaultString("Original fault string");
    ctFaultBean.setOriginalDescription("Original description");

    Fault result = basePaymentService.setFaultDetails(ctFaultBean);

    assertNotNull(result);
    assertEquals("FAULT_ID_123", result.getId());
    assertEquals("PAA_PAGAMENTO_DUPLICATO", result.getFaultCode());
    assertEquals("Original fault string", result.getFaultString());
    assertEquals("Il pagamento è già stato processato", result.getDescription());
    assertEquals(12345, result.getSerial());
    assertEquals("ORIGINAL_CODE_123", result.getOriginalFaultCode());
    assertEquals("Original fault string", result.getOriginalFaultString());
    assertEquals("Original description", result.getOriginalDescription());
  }

  @Test
  void testSetFaultDetails_MinimalFields() {
    CtFaultBean ctFaultBean = new CtFaultBean();
    ctFaultBean.setFaultCode("PPT_ERRORE_GENERICO");

    Fault result = basePaymentService.setFaultDetails(ctFaultBean);

    assertNotNull(result);
    assertNull(result.getId());
    assertEquals("PPT_ERRORE_GENERICO", result.getFaultCode());
    assertNull(result.getFaultString());
    assertNull(result.getDescription());
    assertNull(result.getSerial());
    assertNull(result.getOriginalFaultCode());
    assertNull(result.getOriginalFaultString());
    assertNull(result.getOriginalDescription());
  }

  @Test
  void testSetFaultDetails_NullOriginalFields() {
    CtFaultBean ctFaultBean = new CtFaultBean();
    ctFaultBean.setId("ID_001");
    ctFaultBean.setFaultCode("PAA_SEMANTICA");
    ctFaultBean.setFaultString("Semantic error");
    ctFaultBean.setDescription("Errore semantico");
    ctFaultBean.setSerial(99);
    // originalFaultCode, originalFaultString, originalDescription are null

    Fault result = basePaymentService.setFaultDetails(ctFaultBean);

    assertNotNull(result);
    assertEquals("ID_001", result.getId());
    assertEquals("PAA_SEMANTICA", result.getFaultCode());
    assertNull(result.getFaultString()); // uses originalFaultString which is null
    assertEquals("Errore semantico", result.getDescription());
    assertEquals(99, result.getSerial());
    assertNull(result.getOriginalFaultCode());
    assertNull(result.getOriginalFaultString());
    assertNull(result.getOriginalDescription());
  }

  @Test
  void testSetFaultDetails_WithDifferentOriginalAndFaultCodes() {
    CtFaultBean ctFaultBean = new CtFaultBean();
    ctFaultBean.setFaultCode("PPT_CANALE_ERRORE");
    ctFaultBean.setOriginalFaultCode("CANALE_INDISPONIBILE");
    ctFaultBean.setFaultString("Standard fault string");
    ctFaultBean.setOriginalFaultString("Original channel error");
    ctFaultBean.setDescription("Channel error description");
    ctFaultBean.setOriginalDescription("Original channel error description");

    Fault result = basePaymentService.setFaultDetails(ctFaultBean);

    assertNotNull(result);
    assertEquals("PPT_CANALE_ERRORE", result.getFaultCode());
    assertEquals("CANALE_INDISPONIBILE", result.getOriginalFaultCode());
    assertEquals("Original channel error", result.getFaultString());
    assertEquals("Channel error description", result.getDescription());
    assertEquals("Original channel error", result.getOriginalFaultString());
    assertEquals("Original channel error description", result.getOriginalDescription());
  }

  @Test
  void testSetFaultDetails_ZeroSerial() {
    CtFaultBean ctFaultBean = new CtFaultBean();
    ctFaultBean.setFaultCode("TEST_CODE");
    ctFaultBean.setSerial(0);

    Fault result = basePaymentService.setFaultDetails(ctFaultBean);

    assertNotNull(result);
    assertEquals(0, result.getSerial());
  }

  @Test
  void testSetFaultDetails_EmptyStrings() {
    CtFaultBean ctFaultBean = new CtFaultBean();
    ctFaultBean.setId("");
    ctFaultBean.setFaultCode("");
    ctFaultBean.setFaultString("");
    ctFaultBean.setDescription("");
    ctFaultBean.setOriginalFaultCode("");
    ctFaultBean.setOriginalFaultString("");
    ctFaultBean.setOriginalDescription("");

    Fault result = basePaymentService.setFaultDetails(ctFaultBean);

    assertNotNull(result);
    assertEquals("", result.getId());
    assertEquals("", result.getFaultCode());
    assertEquals("", result.getFaultString());
    assertEquals("", result.getDescription());
    assertEquals("", result.getOriginalFaultCode());
    assertEquals("", result.getOriginalFaultString());
    assertEquals("", result.getOriginalDescription());
  }
}
