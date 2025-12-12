package it.gov.pagopa.miladapter.services.impl;

import static it.gov.pagopa.miladapter.util.PaymentTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.model.QrCode;
import it.gov.pagopa.miladapter.services.model.ActivatePaymentNoticeRequest;
import it.gov.pagopa.miladapter.services.model.ActivatePaymentNoticeResponse;
import it.gov.pagopa.miladapter.services.model.CommonHeader;
import it.gov.pagopa.miladapter.util.ErrorCode;
import it.gov.pagopa.miladapter.util.NodeApi;
import it.gov.pagopa.miladapter.util.PaymentTestData;
import it.gov.pagopa.miladapter.util.QrCodeParser;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
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
class ActivatePaymentNoticeServiceTest {

	@Mock
	private QrCodeParser qrCodeParser;

	@Mock
	private BasePaymentService basePaymentService;

	@InjectMocks
	private ActivatePaymentNoticeService activatePaymentNoticeService;

	private CommonHeader commonHeader;
	private ActivatePaymentNoticeV2Response activatePaymentNoticeV2ResponseOk;
	private ActivatePaymentNoticeRequest activatePaymentNoticeRequest;
	private PspConfiguration pspConfiguration;
	private String encodedQrCode;
	private QrCode parsedQrCode;

	private static final String PAYMENT_TOKEN = "a3b4c5d6e7f8g9h0";

	@BeforeEach
	void setup() {

		// Common headers
		commonHeader = PaymentTestData.getCommonHeader();

		// Encoded valid qr-code
		byte[] bytes = Base64.getUrlEncoder().withoutPadding().encode(QR_CODE.getBytes(StandardCharsets.UTF_8));
		encodedQrCode = new String(bytes, StandardCharsets.UTF_8);

		// Parsed QR code
		parsedQrCode = new QrCode();
		parsedQrCode.setPaTaxCode(PA_TAX_CODE);
		parsedQrCode.setNoticeNumber(NOTICE_NUMBER);

		// PSP configuration
		pspConfiguration = PaymentTestData.getPspConfiguration();

		// Activate request
		activatePaymentNoticeRequest = PaymentTestData.getActivatePaymentRequest();

        // Node activate response OK
        CtTransferPSPV2 transfer = new CtTransferPSPV2();
		transfer.setFiscalCodePA(PA_TAX_CODE);
		transfer.setCompanyName("Test Company");
        transfer.setIBAN("IT0000000000000000000000000");
        transfer.setIdTransfer(1);
        transfer.setTransferAmount(BigDecimal.valueOf(PaymentTestData.AMOUNT,2));
        transfer.setRemittanceInformation("Pagamento di Test");
        transfer.setTransferCategory("Categoria di Test");

        CtTransferListPSPV2 transferList = new CtTransferListPSPV2();
		transferList.getTransfer().add(transfer);

		activatePaymentNoticeV2ResponseOk = new ActivatePaymentNoticeV2Response();
		activatePaymentNoticeV2ResponseOk.setOutcome(StOutcome.OK);
		activatePaymentNoticeV2ResponseOk.setPaymentToken(PAYMENT_TOKEN);
		activatePaymentNoticeV2ResponseOk.setTotalAmount(BigDecimal.valueOf(PaymentTestData.AMOUNT,2));
		activatePaymentNoticeV2ResponseOk.setPaymentDescription("Pagamento di Test");
		activatePaymentNoticeV2ResponseOk.setFiscalCodePA(PA_TAX_CODE);
		activatePaymentNoticeV2ResponseOk.setCompanyName("companyName");
		activatePaymentNoticeV2ResponseOk.setOfficeName("officeName");
		activatePaymentNoticeV2ResponseOk.setTransferList(transferList);
	}

	private ActivatePaymentNoticeV2Response generateKoNodeResponse(String faultCode, String originalFaultCode) {
		CtFaultBean ctFaultBean = new CtFaultBean();
		ctFaultBean.setFaultCode(faultCode);
		ctFaultBean.setOriginalFaultCode(originalFaultCode);

		ActivatePaymentNoticeV2Response activateResponse = new ActivatePaymentNoticeV2Response();
		activateResponse.setOutcome(StOutcome.KO);
		activateResponse.setFault(ctFaultBean);

		return activateResponse;
	}

	@Test
	void testActivateByQrCode_Success() {
		// Arrange
		when(qrCodeParser.b64UrlParse(encodedQrCode)).thenReturn(parsedQrCode);
		when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
				.thenReturn(pspConfiguration);
		when(basePaymentService.activatePaymentNoticeV2(any(ActivatePaymentNoticeV2Request.class)))
				.thenReturn(activatePaymentNoticeV2ResponseOk);

		// Act
		ResponseEntity<ActivatePaymentNoticeResponse> response =
				activatePaymentNoticeService.activateByQrCode(commonHeader, encodedQrCode, activatePaymentNoticeRequest);

		// Assert
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("OK", response.getBody().getOutcome());
        assertEquals(
                activatePaymentNoticeV2ResponseOk
                        .getTotalAmount()
                        .multiply(new BigDecimal(100))
                        .longValue(),
                response.getBody().getAmount().longValue());
		assertEquals(PAYMENT_TOKEN, response.getBody().getPaymentToken());
		assertEquals(PA_TAX_CODE, response.getBody().getPaTaxCode());
		assertNotNull(response.getBody().getTransfers());
		assertEquals(1, response.getBody().getTransfers().size());

		// Verify interactions
		verify(qrCodeParser).b64UrlParse(encodedQrCode);
		verify(basePaymentService).retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/node_error_mapping.csv", numLinesToSkip = 1)
	void testActivateByQrCode_NodeKo(String faultCode, String originalFaultCode, String milOutcome) {
		// Arrange
		when(qrCodeParser.b64UrlParse(encodedQrCode)).thenReturn(parsedQrCode);
		when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
				.thenReturn(pspConfiguration);
		when(basePaymentService.activatePaymentNoticeV2(any(ActivatePaymentNoticeV2Request.class)))
				.thenReturn(generateKoNodeResponse(faultCode, originalFaultCode));
		when(basePaymentService.remapNodeFaultToOutcome(faultCode, originalFaultCode))
				.thenReturn(milOutcome);

		// Act
		ResponseEntity<ActivatePaymentNoticeResponse> response =
				activatePaymentNoticeService.activateByQrCode(commonHeader, encodedQrCode, activatePaymentNoticeRequest);

		// Assert
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(milOutcome, response.getBody().getOutcome());
		assertNull(response.getBody().getAmount());
		assertNull(response.getBody().getPaymentToken());

		verify(basePaymentService).remapNodeFaultToOutcome(faultCode, originalFaultCode);
	}

	@Test
	void testActivateByQrCode_NodeError() {
		// Arrange
		when(qrCodeParser.b64UrlParse(encodedQrCode)).thenReturn(parsedQrCode);
		when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
				.thenReturn(pspConfiguration);
		when(basePaymentService.activatePaymentNoticeV2(any(ActivatePaymentNoticeV2Request.class)))
				.thenThrow(new RuntimeException("Node error"));

		// Act & Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
				activatePaymentNoticeService.activateByQrCode(commonHeader, encodedQrCode, activatePaymentNoticeRequest)
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertNotNull(exception.getReason());
		assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
	}

	@Test
	void testActivateByQrCode_NodeReturnsNull() {
		// Arrange
		when(qrCodeParser.b64UrlParse(encodedQrCode)).thenReturn(parsedQrCode);
		when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
				.thenReturn(pspConfiguration);
		when(basePaymentService.activatePaymentNoticeV2(any(ActivatePaymentNoticeV2Request.class)))
				.thenReturn(null);

		// Act & Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
				activatePaymentNoticeService.activateByQrCode(commonHeader, encodedQrCode, activatePaymentNoticeRequest)
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertNotNull(exception.getReason());
		assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
	}

	@Test
	void testActivateByTaxCodeAndNoticeNumber_Success() {
		// Arrange
		when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
				.thenReturn(pspConfiguration);
		when(basePaymentService.activatePaymentNoticeV2(any(ActivatePaymentNoticeV2Request.class)))
				.thenReturn(activatePaymentNoticeV2ResponseOk);

		// Act
		ResponseEntity<ActivatePaymentNoticeResponse> response =
				activatePaymentNoticeService.activateByTaxCodeAndNoticeNumber(
						commonHeader, PA_TAX_CODE, NOTICE_NUMBER, activatePaymentNoticeRequest);

		// Assert
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("OK", response.getBody().getOutcome());
		assertEquals(AMOUNT, response.getBody().getAmount().longValue());
		assertEquals(PAYMENT_TOKEN, response.getBody().getPaymentToken());

		ArgumentCaptor<ActivatePaymentNoticeV2Request> captorActivateReq = ArgumentCaptor.forClass(ActivatePaymentNoticeV2Request.class);
		verify(basePaymentService).activatePaymentNoticeV2(captorActivateReq.capture());
		assertEquals(PA_TAX_CODE, captorActivateReq.getValue().getQrCode().getFiscalCode());
		assertEquals(NOTICE_NUMBER, captorActivateReq.getValue().getQrCode().getNoticeNumber());
	}

	@Test
	void testActivateByTaxCodeAndNoticeNumber_NodeKo() {
		// Arrange
		String faultCode = "PPT_PAGAMENTO_DUPLICATO";
		String originalFaultCode = null;
		String expectedOutcome = "NOTICE_ALREADY_PAID";

		when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
				.thenReturn(pspConfiguration);
		when(basePaymentService.activatePaymentNoticeV2(any(ActivatePaymentNoticeV2Request.class)))
				.thenReturn(generateKoNodeResponse(faultCode, originalFaultCode));
		when(basePaymentService.remapNodeFaultToOutcome(faultCode, originalFaultCode))
				.thenReturn(expectedOutcome);

		// Act
		ResponseEntity<ActivatePaymentNoticeResponse> response =
				activatePaymentNoticeService.activateByTaxCodeAndNoticeNumber(
						commonHeader, PA_TAX_CODE, NOTICE_NUMBER, activatePaymentNoticeRequest);

		// Assert
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedOutcome, response.getBody().getOutcome());
		assertNull(response.getBody().getAmount());
		assertNull(response.getBody().getPaymentToken());
	}

	@Test
	void testActivateByTaxCodeAndNoticeNumber_NodeError() {
		// Arrange
		when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
				.thenReturn(pspConfiguration);
		when(basePaymentService.activatePaymentNoticeV2(any(ActivatePaymentNoticeV2Request.class)))
				.thenThrow(new RuntimeException("Node connection error"));

		// Act & Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
				activatePaymentNoticeService.activateByTaxCodeAndNoticeNumber(
						commonHeader, PA_TAX_CODE, NOTICE_NUMBER, activatePaymentNoticeRequest)
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertNotNull(exception.getReason());
		assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
	}

	@Test
	void testActivateByTaxCodeAndNoticeNumber_ConfigurationError() {
		// Arrange
		when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.VERIFY))
				.thenThrow(new ResponseStatusException(
						HttpStatus.INTERNAL_SERVER_ERROR,
						"Configuration error"));

		// Act & Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
				activatePaymentNoticeService.activateByTaxCodeAndNoticeNumber(
						commonHeader, PA_TAX_CODE, NOTICE_NUMBER, activatePaymentNoticeRequest)
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		verify(basePaymentService, never()).activatePaymentNoticeV2(any());
	}
}

