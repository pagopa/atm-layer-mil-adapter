package it.gov.pagopa.miladapter.services.impl;

import static it.gov.pagopa.miladapter.util.PaymentTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.model.QrCode;
import it.gov.pagopa.miladapter.services.model.CommonHeader;
import it.gov.pagopa.miladapter.services.model.VerifyPaymentNoticeResponse;
import it.gov.pagopa.miladapter.util.ErrorCode;
import it.gov.pagopa.miladapter.util.NodeApi;
import it.gov.pagopa.miladapter.util.PaymentTestData;
import it.gov.pagopa.miladapter.util.QrCodeParser;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@Disabled
class VerifyPaymentNoticeServiceTest {

  @Mock private QrCodeParser qrCodeParser;

  @Mock private BasePaymentService basePaymentService;

  @InjectMocks private VerifyPaymentNoticeService verifyPaymentNoticeService;

  private CommonHeader commonHeader;
  private VerifyPaymentNoticeRes verifyPaymentNoticeResOk;
  private PspConfiguration pspConfiguration;
  private String encodedQrCode;
  private QrCode parsedQrCode;

  @BeforeEach
  void setup() {
    // Common headers
    commonHeader = PaymentTestData.getCommonHeader();

    // Encoded valid qr-code
    byte[] bytes =
        Base64.getUrlEncoder().withoutPadding().encode(QR_CODE.getBytes(StandardCharsets.UTF_8));
    encodedQrCode = new String(bytes, StandardCharsets.UTF_8);

    // Parsed QR code
    parsedQrCode = new QrCode();
    parsedQrCode.setPaTaxCode(PA_TAX_CODE);
    parsedQrCode.setNoticeNumber(NOTICE_NUMBER);

    // PSP configuration
    pspConfiguration = PaymentTestData.getPspConfiguration();

    // Node verify response OK
    XMLGregorianCalendar dueDate = null;
    try {
      GregorianCalendar gregorianCalendar = new GregorianCalendar();
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      gregorianCalendar.setTime(formatter.parse("2021-07-31"));
      dueDate =
          DatatypeFactory.newInstance()
              .newXMLGregorianCalendarDate(
                  gregorianCalendar.get(Calendar.YEAR),
                  gregorianCalendar.get(Calendar.MONTH) + 1,
                  gregorianCalendar.get(Calendar.DAY_OF_MONTH),
                  DatatypeConstants.FIELD_UNDEFINED);
    } catch (ParseException | DatatypeConfigurationException ignored) {
    }

    CtPaymentOptionDescription paymentDescription = new CtPaymentOptionDescription();
    paymentDescription.setAmount(new BigDecimal("100.99"));
    paymentDescription.setOptions(StAmountOptionPSP.EQ);
    paymentDescription.setDueDate(dueDate);
    paymentDescription.setPaymentNote("paymentNote");

    CtPaymentOptionsDescriptionList paymentList = new CtPaymentOptionsDescriptionList();
    paymentList.getPaymentOptionDescription().add(paymentDescription);

    verifyPaymentNoticeResOk = new VerifyPaymentNoticeRes();
    verifyPaymentNoticeResOk.setOutcome(StOutcome.OK);
    verifyPaymentNoticeResOk.setPaymentList(paymentList);
    verifyPaymentNoticeResOk.setPaymentDescription("Pagamento di Test");
    verifyPaymentNoticeResOk.setFiscalCodePA(PA_TAX_CODE);
    verifyPaymentNoticeResOk.setOfficeName("officeName");
    verifyPaymentNoticeResOk.setCompanyName("companyName");
  }

  private VerifyPaymentNoticeRes generateKoNodeResponse(
      String faultCode, String originalFaultCode) {
    CtFaultBean ctFaultBean = new CtFaultBean();
    ctFaultBean.setFaultCode(faultCode);
    ctFaultBean.setOriginalFaultCode(originalFaultCode);

    VerifyPaymentNoticeRes verifyPaymentNoticeRes = new VerifyPaymentNoticeRes();
    verifyPaymentNoticeRes.setOutcome(StOutcome.KO);
    verifyPaymentNoticeRes.setFault(ctFaultBean);

    return verifyPaymentNoticeRes;
  }

  @Test
  void testVerifyByQrCode_Success() {
    // Arrange
    when(qrCodeParser.b64UrlParse(encodedQrCode)).thenReturn(parsedQrCode);
    when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
        .thenReturn(pspConfiguration);
    when(basePaymentService.verifyPaymentNotice(any(VerifyPaymentNoticeReq.class)))
        .thenReturn(verifyPaymentNoticeResOk);

    // Act
    ResponseEntity<VerifyPaymentNoticeResponse> response =
        verifyPaymentNoticeService.verifyByQrCode(commonHeader, encodedQrCode);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("OK", response.getBody().getOutcome());
    assertEquals(
        verifyPaymentNoticeResOk
            .getPaymentList()
            .getPaymentOptionDescription()
            .getFirst()
            .getAmount()
            .multiply(new BigDecimal(100))
            .longValue(),
        response.getBody().getAmount().longValue());
    assertEquals("2021-07-31", response.getBody().getDueDate());
    assertEquals("paymentNote", response.getBody().getNote());
    assertEquals("Pagamento di Test", response.getBody().getDescription());
    assertEquals("companyName", response.getBody().getCompany());
    assertEquals("officeName", response.getBody().getOffice());
    assertEquals(PA_TAX_CODE, response.getBody().getPaTaxCode());
    assertEquals(NOTICE_NUMBER, response.getBody().getNoticeNumber());

    // Verify interactions
    verify(qrCodeParser).b64UrlParse(encodedQrCode);
    verify(basePaymentService).retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY);

    ArgumentCaptor<VerifyPaymentNoticeReq> captorVerifyReq =
        ArgumentCaptor.forClass(VerifyPaymentNoticeReq.class);
    verify(basePaymentService).verifyPaymentNotice(captorVerifyReq.capture());
    assertEquals("97735020584", captorVerifyReq.getValue().getIdBrokerPSP());
    assertEquals(PA_TAX_CODE, captorVerifyReq.getValue().getQrCode().getFiscalCode());
    assertEquals(NOTICE_NUMBER, captorVerifyReq.getValue().getQrCode().getNoticeNumber());
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/node_error_mapping.csv", numLinesToSkip = 1)
  void testVerifyByQrCode_NodeKo(String faultCode, String originalFaultCode, String milOutcome) {
    // Arrange
    when(qrCodeParser.b64UrlParse(encodedQrCode)).thenReturn(parsedQrCode);
    when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
        .thenReturn(pspConfiguration);
    when(basePaymentService.verifyPaymentNotice(any(VerifyPaymentNoticeReq.class)))
        .thenReturn(generateKoNodeResponse(faultCode, originalFaultCode));
    when(basePaymentService.remapNodeFaultToOutcome(faultCode, originalFaultCode))
        .thenReturn(milOutcome);

    // Act
    ResponseEntity<VerifyPaymentNoticeResponse> response =
        verifyPaymentNoticeService.verifyByQrCode(commonHeader, encodedQrCode);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(milOutcome, response.getBody().getOutcome());
    assertNull(response.getBody().getAmount());
    assertNull(response.getBody().getDueDate());

    verify(basePaymentService).remapNodeFaultToOutcome(faultCode, originalFaultCode);
  }

  @Test
  void testVerifyByQrCode_NodeError() {
    // Arrange
    when(qrCodeParser.b64UrlParse(encodedQrCode)).thenReturn(parsedQrCode);
    when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
        .thenReturn(pspConfiguration);
    when(basePaymentService.verifyPaymentNotice(any(VerifyPaymentNoticeReq.class)))
        .thenThrow(new RuntimeException("Node error"));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> verifyPaymentNoticeService.verifyByQrCode(commonHeader, encodedQrCode));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertNotNull(exception.getReason());
    assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
  }

  @Test
  void testVerifyByQrCode_NodeReturnsNull() {
    // Arrange
    when(qrCodeParser.b64UrlParse(encodedQrCode)).thenReturn(parsedQrCode);
    when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
        .thenReturn(pspConfiguration);
    when(basePaymentService.verifyPaymentNotice(any(VerifyPaymentNoticeReq.class)))
        .thenReturn(null);

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> verifyPaymentNoticeService.verifyByQrCode(commonHeader, encodedQrCode));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertNotNull(exception.getReason());
    assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
  }

  @Test
  void testVerifyByTaxCodeAndNoticeNumber_Success() {
    // Arrange
    when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
        .thenReturn(pspConfiguration);
    when(basePaymentService.verifyPaymentNotice(any(VerifyPaymentNoticeReq.class)))
        .thenReturn(verifyPaymentNoticeResOk);

    // Act
    ResponseEntity<VerifyPaymentNoticeResponse> response =
        verifyPaymentNoticeService.verifyByTaxCodeAndNoticeNumber(
            commonHeader, PA_TAX_CODE, NOTICE_NUMBER);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("OK", response.getBody().getOutcome());
    assertEquals(10099, response.getBody().getAmount().intValue());

    ArgumentCaptor<VerifyPaymentNoticeReq> captorVerifyReq =
        ArgumentCaptor.forClass(VerifyPaymentNoticeReq.class);
    verify(basePaymentService).verifyPaymentNotice(captorVerifyReq.capture());
    assertEquals(PA_TAX_CODE, captorVerifyReq.getValue().getQrCode().getFiscalCode());
    assertEquals(NOTICE_NUMBER, captorVerifyReq.getValue().getQrCode().getNoticeNumber());
  }

  @Test
  void testVerifyByTaxCodeAndNoticeNumber_NodeKo() {
    // Arrange
    String faultCode = "PPT_DOMINIO_SCONOSCIUTO";
    String originalFaultCode = null;
    String expectedOutcome = "WRONG_NOTICE_DATA";

    when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
        .thenReturn(pspConfiguration);
    when(basePaymentService.verifyPaymentNotice(any(VerifyPaymentNoticeReq.class)))
        .thenReturn(generateKoNodeResponse(faultCode, originalFaultCode));
    when(basePaymentService.remapNodeFaultToOutcome(faultCode, originalFaultCode))
        .thenReturn(expectedOutcome);

    // Act
    ResponseEntity<VerifyPaymentNoticeResponse> response =
        verifyPaymentNoticeService.verifyByTaxCodeAndNoticeNumber(
            commonHeader, PA_TAX_CODE, NOTICE_NUMBER);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(expectedOutcome, response.getBody().getOutcome());
    assertNull(response.getBody().getAmount());
  }

  @Test
  void testVerifyByTaxCodeAndNoticeNumber_NodeError() {
    // Arrange
    when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
        .thenReturn(pspConfiguration);
    when(basePaymentService.verifyPaymentNotice(any(VerifyPaymentNoticeReq.class)))
        .thenThrow(new RuntimeException("Node connection error"));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () ->
                verifyPaymentNoticeService.verifyByTaxCodeAndNoticeNumber(
                    commonHeader, PA_TAX_CODE, NOTICE_NUMBER));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertNotNull(exception.getReason());
    assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
  }
}
