package it.gov.pagopa.miladapter.services;

import static it.gov.pagopa.miladapter.util.PaymentTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.services.impl.BasePaymentService;
import it.gov.pagopa.miladapter.services.impl.PaymentService;
import it.gov.pagopa.miladapter.services.model.ClosePaymentRequest;
import it.gov.pagopa.miladapter.services.model.ClosePaymentResponse;
import it.gov.pagopa.miladapter.util.ErrorCode;
import it.gov.pagopa.miladapter.util.NodeApi;
import it.gov.pagopa.miladapter.util.PaymentTestData;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Response;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.pagopa.swclient.mil.bean.CommonHeader;
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
import reactor.core.publisher.Mono;

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
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
                .thenReturn(Mono.just(pspConfiguration));
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(Mono.just(sendPaymentOutcomeV2Response));

        ResponseEntity<ClosePaymentResponse> response =
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk);

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        verify(basePaymentService).retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE);

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
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
                .thenReturn(Mono.just(pspConfiguration));
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(Mono.just(sendPaymentOutcomeV2Response));

        ResponseEntity<ClosePaymentResponse> response =
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestKo);

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

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

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
                .thenReturn(Mono.just(pspConfiguration));
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(Mono.just(sendPaymentOutcomeV2Response));

        ResponseEntity<ClosePaymentResponse> response =
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk);

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        ArgumentCaptor<SendPaymentOutcomeV2Request> captorSendPaymentOutcome =
                ArgumentCaptor.forClass(SendPaymentOutcomeV2Request.class);
        verify(basePaymentService).sendPaymentOutcomeV2(captorSendPaymentOutcome.capture());

        SendPaymentOutcomeV2Request capturedRequest = captorSendPaymentOutcome.getValue();
        assertEquals(3, capturedRequest.getPaymentTokens().getPaymentToken().size());
        assertTrue(capturedRequest.getPaymentTokens().getPaymentToken().containsAll(multipleTokens));
    }

    @Test
    void testSendPaymentOutcome_ConfigurationError() {
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
                .thenReturn(Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Configuration error")));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verify(basePaymentService, never()).sendPaymentOutcomeV2(any());
    }

    @Test
    void testSendPaymentOutcome_NodeError() {
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
                .thenReturn(Mono.just(pspConfiguration));
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(Mono.error(new RuntimeException("Node connection error")));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
    }

    @Test
    void testSendPaymentOutcome_NodeReturnsNull() {
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
                .thenReturn(Mono.just(pspConfiguration));
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(Mono.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                paymentService.sendPaymentOutcome(commonHeader, TRANSACTION_ID, closePaymentRequestOk)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES));
    }

    @Test
    void testSendPaymentOutcome_NodeThrowsException() {
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
                .thenReturn(Mono.just(pspConfiguration));
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

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.ACTIVATE))
                .thenReturn(Mono.just(customPspConfig));
        when(basePaymentService.sendPaymentOutcomeV2(any(SendPaymentOutcomeV2Request.class)))
                .thenReturn(Mono.just(sendPaymentOutcomeV2Response));

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
}

