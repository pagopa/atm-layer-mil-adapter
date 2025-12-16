package it.gov.pagopa.miladapter.services.impl;

import static it.gov.pagopa.miladapter.util.PaymentTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.services.model.ClosePaymentRequest;
import it.gov.pagopa.miladapter.services.model.ClosePaymentResponse;
import it.gov.pagopa.miladapter.services.model.CommonHeader;
import it.gov.pagopa.miladapter.services.model.Fault;
import it.gov.pagopa.miladapter.util.ErrorCode;
import it.gov.pagopa.miladapter.util.PaymentTestData;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Response;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private BasePaymentService basePaymentService;

    @InjectMocks
    private PaymentService paymentService;

    private CommonHeader commonHeader;
    private PspConfiguration pspConfiguration;
    private ClosePaymentRequest closePaymentRequestOk;
    private ClosePaymentRequest closePaymentRequestKo;
    private SendPaymentOutcomeV2Response sendPaymentOutcomeV2Response;


    @BeforeEach
    void setup() {
        // Common headers
        commonHeader = PaymentTestData.getCommonHeader();

        // PSP configuration
        pspConfiguration = PaymentTestData.getPspConfiguration();

        // Close payment requests
        closePaymentRequestOk = PaymentTestData.getClosePaymentRequest(true);
        closePaymentRequestOk.setPaymentTokens(List.of(PAYMENT_TOKEN));

        closePaymentRequestKo = PaymentTestData.getClosePaymentRequest(false);
        closePaymentRequestKo.setPaymentTokens(List.of(PAYMENT_TOKEN));

        // Node response
        sendPaymentOutcomeV2Response = new SendPaymentOutcomeV2Response();
        sendPaymentOutcomeV2Response.setOutcome(StOutcome.OK);
    }

    @Test
    void testSendPaymentOutcome_Success_OutcomeClose() {
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenReturn(pspConfiguration);
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(sendPaymentOutcomeV2Response);

        ResponseEntity<ClosePaymentResponse> response =
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(basePaymentService).retrievePSPConfiguration(ACQUIRER_ID);

        ArgumentCaptor<SendPaymentOutcomeV2Request> captorSendPaymentOutcome =
                ArgumentCaptor.forClass(SendPaymentOutcomeV2Request.class);
        verify(basePaymentService).sendPaymentOutcomeV2(captorSendPaymentOutcome.capture());

        SendPaymentOutcomeV2Request capturedRequest = captorSendPaymentOutcome.getValue();
        assertEquals(pspConfiguration.getPsp(), capturedRequest.getIdPSP());
        assertEquals(pspConfiguration.getBroker(), capturedRequest.getIdBrokerPSP());
        assertEquals(pspConfiguration.getChannel(), capturedRequest.getIdChannel());
        assertEquals(pspConfiguration.getPassword(), capturedRequest.getPassword());
        assertEquals(StOutcome.OK, capturedRequest.getOutcome());
        assertEquals(1, capturedRequest.getPaymentTokens().getPaymentToken().size());
        assertEquals(PAYMENT_TOKEN, capturedRequest.getPaymentTokens().getPaymentToken().getFirst());
    }

    @Test
    void testSendPaymentOutcome_Success_OutcomeError() {
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenReturn(pspConfiguration);
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(sendPaymentOutcomeV2Response);

        ResponseEntity<ClosePaymentResponse> response =
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestKo);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<SendPaymentOutcomeV2Request> captorSendPaymentOutcome =
                ArgumentCaptor.forClass(SendPaymentOutcomeV2Request.class);
        verify(basePaymentService).sendPaymentOutcomeV2(captorSendPaymentOutcome.capture());

        SendPaymentOutcomeV2Request capturedRequest = captorSendPaymentOutcome.getValue();
        assertEquals(StOutcome.KO, capturedRequest.getOutcome());
    }

    @Test
    void testSendPaymentOutcome_MultiplePaymentTokens() {
        List<String> multipleTokens = List.of(PAYMENT_TOKEN, "token2", "token3");
        closePaymentRequestOk.setPaymentTokens(multipleTokens);

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenReturn(pspConfiguration);
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(sendPaymentOutcomeV2Response);

        ResponseEntity<ClosePaymentResponse> response =
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<SendPaymentOutcomeV2Request> captorSendPaymentOutcome =
                ArgumentCaptor.forClass(SendPaymentOutcomeV2Request.class);
        verify(basePaymentService).sendPaymentOutcomeV2(captorSendPaymentOutcome.capture());

        SendPaymentOutcomeV2Request capturedRequest = captorSendPaymentOutcome.getValue();
        assertEquals(3, capturedRequest.getPaymentTokens().getPaymentToken().size());
        assertTrue(capturedRequest.getPaymentTokens().getPaymentToken().containsAll(multipleTokens));
    }

    @Test
    void testSendPaymentOutcome_ConfigurationError() {
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Configuration error"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verify(basePaymentService, never()).sendPaymentOutcomeV2(any());
    }

    @Test
    void testSendPaymentOutcome_NodeError() {
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenReturn(pspConfiguration);
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenThrow(new RuntimeException("Node connection error"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
    }

    @Test
    void testSendPaymentOutcome_NodeReturnsNull() {
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenReturn(pspConfiguration);
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
    }

    @Test
    void testSendPaymentOutcome_NodeThrowsException() {
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenReturn(pspConfiguration);
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
    }

    @Test
    void testSendPaymentOutcome_VerifyPspConfigurationMapping() {
        PspConfiguration customPspConfig = new PspConfiguration();
        customPspConfig.setPsp("CUSTOM_PSP");
        customPspConfig.setBroker("CUSTOM_BROKER");
        customPspConfig.setChannel("CUSTOM_CHANNEL");
        customPspConfig.setPassword("CUSTOM_PASSWORD");

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenReturn(customPspConfig);
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(sendPaymentOutcomeV2Response);

        paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk);

        // verify custom PSP configuration is used
        ArgumentCaptor<SendPaymentOutcomeV2Request> captorSendPaymentOutcome =
                ArgumentCaptor.forClass(SendPaymentOutcomeV2Request.class);
        verify(basePaymentService).sendPaymentOutcomeV2(captorSendPaymentOutcome.capture());

        SendPaymentOutcomeV2Request capturedRequest = captorSendPaymentOutcome.getValue();
        assertEquals("CUSTOM_PSP", capturedRequest.getIdPSP());
        assertEquals("CUSTOM_BROKER", capturedRequest.getIdBrokerPSP());
        assertEquals("CUSTOM_CHANNEL", capturedRequest.getIdChannel());
        assertEquals("CUSTOM_PASSWORD", capturedRequest.getPassword());
    }

    @Test
    void testSendPaymentOutcome_NodeReturnsKoWithFault() {
        // Prepare node response with KO outcome and fault
        SendPaymentOutcomeV2Response koResponse = new SendPaymentOutcomeV2Response();
        koResponse.setOutcome(StOutcome.KO);

        CtFaultBean ctFaultBean = new CtFaultBean();
        ctFaultBean.setFaultCode("PAA_PAGAMENTO_DUPLICATO");
        ctFaultBean.setOriginalFaultCode("PAA_PAGAMENTO_DUPLICATO");
        ctFaultBean.setFaultString("Payment already processed");
        ctFaultBean.setDescription("Il pagamento è già stato processato");
        koResponse.setFault(ctFaultBean);

        Fault expectedFault = new Fault();
        expectedFault.setFaultCode("PAA_PAGAMENTO_DUPLICATO");
        expectedFault.setDescription("Il pagamento è già stato processato");

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenReturn(pspConfiguration);
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(koResponse);
        when(basePaymentService.remapNodeFaultToOutcome("PAA_PAGAMENTO_DUPLICATO", "PAA_PAGAMENTO_DUPLICATO"))
                .thenReturn("PAYMENT_DUPLICATED");
        when(basePaymentService.setFaultDetails(ctFaultBean))
                .thenReturn(expectedFault);

        ResponseEntity<ClosePaymentResponse> response =
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PAYMENT_DUPLICATED", response.getBody().getOutcome());
        assertNotNull(response.getBody().getFault());
        assertEquals("PAA_PAGAMENTO_DUPLICATO", response.getBody().getFault().getFaultCode());
        assertEquals("Il pagamento è già stato processato", response.getBody().getFault().getDescription());

        verify(basePaymentService).remapNodeFaultToOutcome("PAA_PAGAMENTO_DUPLICATO", "PAA_PAGAMENTO_DUPLICATO");
        verify(basePaymentService).setFaultDetails(ctFaultBean);
    }

    @Test
    void testSendPaymentOutcome_NodeReturnsKoWithDifferentFaultCodes() {
        SendPaymentOutcomeV2Response koResponse = new SendPaymentOutcomeV2Response();
        koResponse.setOutcome(StOutcome.KO);

        CtFaultBean ctFaultBean = new CtFaultBean();
        ctFaultBean.setFaultCode("PPT_CANALE_ERRORE");
        ctFaultBean.setOriginalFaultCode("ORIGINAL_ERROR");
        ctFaultBean.setFaultString("Channel error");
        koResponse.setFault(ctFaultBean);

        Fault expectedFault = new Fault();
        expectedFault.setFaultCode("PPT_CANALE_ERRORE");

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenReturn(pspConfiguration);
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(koResponse);
        when(basePaymentService.remapNodeFaultToOutcome("PPT_CANALE_ERRORE", "ORIGINAL_ERROR"))
                .thenReturn("GENERIC_ERROR");
        when(basePaymentService.setFaultDetails(ctFaultBean))
                .thenReturn(expectedFault);

        ResponseEntity<ClosePaymentResponse> response =
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("GENERIC_ERROR", response.getBody().getOutcome());
        assertNotNull(response.getBody().getFault());

        verify(basePaymentService).remapNodeFaultToOutcome("PPT_CANALE_ERRORE", "ORIGINAL_ERROR");
        verify(basePaymentService).setFaultDetails(ctFaultBean);
    }

    @Test
    void testSendPaymentOutcome_NodeReturnsKoWithMinimalFaultInfo() {
        SendPaymentOutcomeV2Response koResponse = new SendPaymentOutcomeV2Response();
        koResponse.setOutcome(StOutcome.KO);

        CtFaultBean ctFaultBean = new CtFaultBean();
        ctFaultBean.setFaultCode("GENERIC_ERROR");
        koResponse.setFault(ctFaultBean);

        Fault expectedFault = new Fault();
        expectedFault.setFaultCode("GENERIC_ERROR");

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID))
                .thenReturn(pspConfiguration);
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(koResponse);
        when(basePaymentService.remapNodeFaultToOutcome(eq("GENERIC_ERROR"), isNull()))
                .thenReturn("UNKNOWN_ERROR");
        when(basePaymentService.setFaultDetails(ctFaultBean))
                .thenReturn(expectedFault);

        ResponseEntity<ClosePaymentResponse> response =
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNKNOWN_ERROR", response.getBody().getOutcome());

        verify(basePaymentService).remapNodeFaultToOutcome(eq("GENERIC_ERROR"), isNull());
    }
}
