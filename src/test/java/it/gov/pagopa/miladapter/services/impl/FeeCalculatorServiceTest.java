package it.gov.pagopa.miladapter.services.impl;

import static it.gov.pagopa.miladapter.util.PaymentTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import it.gov.pagopa.miladapter.client.model.GecGetFeesRequest;
import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.properties.GecProperties;
import it.gov.pagopa.miladapter.services.model.*;
import it.gov.pagopa.miladapter.util.FeeCalculatorErrorCode;
import it.gov.pagopa.miladapter.util.NodeApi;
import it.gov.pagopa.miladapter.util.PaymentTestData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
class FeeCalculatorServiceTest {

    @Mock
    private BasePaymentService basePaymentService;

    @Mock
    private GecProperties gecProperties;

    @InjectMocks
    private FeeCalculatorService feeCalculatorService;

    private CommonHeader commonHeader;
    private GetFeeRequest getFeeRequest;
    private PspConfiguration pspConfiguration;
    private GetFeeResponse getFeeResponse;

    @BeforeEach
    void setup() {
        // Common headers
        commonHeader = PaymentTestData.getCommonHeader();

        // PSP configuration
        pspConfiguration = PaymentTestData.getPspConfiguration();

        // GetFeeRequest
        getFeeRequest = PaymentTestData.getFeeRequest();

        // GetFeeResponse
        getFeeResponse = new GetFeeResponse();
        getFeeResponse.setFee(FEE);

        // Mock GecProperties
        GecProperties.Touchpoint touchpoint = new GecProperties.Touchpoint();
        Map<String, String> touchpointMap = new HashMap<>();
        touchpointMap.put(CHANNEL, "ATM");
        touchpoint.setMap(touchpointMap);

        GecProperties.PaymentMethod paymentMethod = new GecProperties.PaymentMethod();
        Map<String, String> paymentMethodMap = new HashMap<>();
        paymentMethodMap.put("PAGOBANCOMAT", "CP");
        paymentMethod.setMap(paymentMethodMap);

        lenient().when(gecProperties.getTouchpoint()).thenReturn(touchpoint);
        lenient().when(gecProperties.getPaymentmethod()).thenReturn(paymentMethod);
    }

    @Test
    void testGetFee_Success() {
        // Arrange
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE))
                .thenReturn(pspConfiguration);
        when(basePaymentService.getFees(eq(commonHeader.getRequestId()), any(GecGetFeesRequest.class)))
                .thenReturn(getFeeResponse);

        // Act
        ResponseEntity<GetFeeResponse> response = feeCalculatorService.getFee(commonHeader, getFeeRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(FEE, response.getBody().getFee());

        // Verify interactions
        verify(basePaymentService).retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE);

        ArgumentCaptor<GecGetFeesRequest> captorGecRequest = ArgumentCaptor.forClass(GecGetFeesRequest.class);
        verify(basePaymentService).getFees(eq(commonHeader.getRequestId()), captorGecRequest.capture());

        GecGetFeesRequest capturedRequest = captorGecRequest.getValue();
        assertEquals(AMOUNT, capturedRequest.getPaymentAmount());
        assertEquals(PA_TAX_CODE, capturedRequest.getPrimaryCreditorInstitution());
        assertEquals("CP", capturedRequest.getPaymentMethod());
        assertEquals("ATM", capturedRequest.getTouchpoint());
        assertEquals(1, capturedRequest.getIdPspList().size());
        assertEquals(PSP_ID, capturedRequest.getIdPspList().getFirst().getIdPsp());
        assertEquals(1, capturedRequest.getTransferList().size());
        assertEquals(PA_TAX_CODE, capturedRequest.getTransferList().getFirst().getCreditorInstitution());
        assertEquals("KTM", capturedRequest.getTransferList().getFirst().getTransferCategory());
    }

    @Test
    void testGetFee_Success_WithDefaultPaymentMethod() {
        // Arrange - payment method not in map, should use default "ANY"
        getFeeRequest.setPaymentMethod("UNKNOWN_METHOD");

        Map<String, String> paymentMethodMap = new HashMap<>();
        paymentMethodMap.put("PAGOBANCOMAT", "CP");
        GecProperties.PaymentMethod paymentMethod = new GecProperties.PaymentMethod();
        paymentMethod.setMap(paymentMethodMap);
        when(gecProperties.getPaymentmethod()).thenReturn(paymentMethod);

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE))
                .thenReturn(pspConfiguration);
        when(basePaymentService.getFees(eq(commonHeader.getRequestId()), any(GecGetFeesRequest.class)))
                .thenReturn(getFeeResponse);

        // Act
        ResponseEntity<GetFeeResponse> response = feeCalculatorService.getFee(commonHeader, getFeeRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<GecGetFeesRequest> captorGecRequest = ArgumentCaptor.forClass(GecGetFeesRequest.class);
        verify(basePaymentService).getFees(eq(commonHeader.getRequestId()), captorGecRequest.capture());
        assertEquals("ANY", captorGecRequest.getValue().getPaymentMethod());
    }

    @Test
    void testGetFee_Success_WithDefaultTouchpoint() {
        // Arrange - channel not in map, should use default "ANY"
        commonHeader.setChannel("UNKNOWN_CHANNEL");

        Map<String, String> touchpointMap = new HashMap<>();
        touchpointMap.put("ATM", "ATM");
        GecProperties.Touchpoint touchpoint = new GecProperties.Touchpoint();
        touchpoint.setMap(touchpointMap);
        when(gecProperties.getTouchpoint()).thenReturn(touchpoint);

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE))
                .thenReturn(pspConfiguration);
        when(basePaymentService.getFees(eq(commonHeader.getRequestId()), any(GecGetFeesRequest.class)))
                .thenReturn(getFeeResponse);

        // Act
        ResponseEntity<GetFeeResponse> response = feeCalculatorService.getFee(commonHeader, getFeeRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<GecGetFeesRequest> captorGecRequest = ArgumentCaptor.forClass(GecGetFeesRequest.class);
        verify(basePaymentService).getFees(eq(commonHeader.getRequestId()), captorGecRequest.capture());
        assertEquals("ANY", captorGecRequest.getValue().getTouchpoint());
    }

    @Test
    void testGetFee_Success_WithEmptyCategory() {
        // Arrange - transfer with empty category should not set transferCategory
        Transfer transferWithoutCategory = new Transfer();
        transferWithoutCategory.setPaTaxCode(PA_TAX_CODE);
        transferWithoutCategory.setCategory("");

        Notice notice = new Notice();
        notice.setPaTaxCode(PA_TAX_CODE);
        notice.setNoticeNumber(NOTICE_NUMBER);
        notice.setAmount(AMOUNT);
        notice.setTransfers(List.of(transferWithoutCategory));

        getFeeRequest.setNotices(List.of(notice));

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE))
                .thenReturn(pspConfiguration);
        when(basePaymentService.getFees(eq(commonHeader.getRequestId()), any(GecGetFeesRequest.class)))
                .thenReturn(getFeeResponse);

        // Act
        ResponseEntity<GetFeeResponse> response = feeCalculatorService.getFee(commonHeader, getFeeRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<GecGetFeesRequest> captorGecRequest = ArgumentCaptor.forClass(GecGetFeesRequest.class);
        verify(basePaymentService).getFees(eq(commonHeader.getRequestId()), captorGecRequest.capture());
        assertNull(captorGecRequest.getValue().getTransferList().getFirst().getTransferCategory());
    }

    @Test
    void testGetFee_Success_WithNullPaymentMethod() {
        // Arrange - null payment method should not set paymentMethod in GEC request
        getFeeRequest.setPaymentMethod(null);

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE))
                .thenReturn(pspConfiguration);
        when(basePaymentService.getFees(eq(commonHeader.getRequestId()), any(GecGetFeesRequest.class)))
                .thenReturn(getFeeResponse);

        // Act
        ResponseEntity<GetFeeResponse> response = feeCalculatorService.getFee(commonHeader, getFeeRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<GecGetFeesRequest> captorGecRequest = ArgumentCaptor.forClass(GecGetFeesRequest.class);
        verify(basePaymentService).getFees(eq(commonHeader.getRequestId()), captorGecRequest.capture());
        assertNull(captorGecRequest.getValue().getPaymentMethod());
    }

    @Test
    void testGetFee_ConfigurationError() {
        // Arrange
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Configuration error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                feeCalculatorService.getFee(commonHeader, getFeeRequest)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verify(basePaymentService, never()).getFees(any(), any());
    }

    @Test
    void testGetFee_GecServiceError() {
        // Arrange
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE))
                .thenReturn(pspConfiguration);
        when(basePaymentService.getFees(eq(commonHeader.getRequestId()), any(GecGetFeesRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        FeeCalculatorErrorCode.ERROR_RETRIEVING_FEES));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                feeCalculatorService.getFee(commonHeader, getFeeRequest)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains(FeeCalculatorErrorCode.ERROR_RETRIEVING_FEES));
    }

    @Test
    void testGetFee_NoFeeFound() {
        // Arrange - BasePaymentService returns error for no fee found
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE))
                .thenReturn(pspConfiguration);
        when(basePaymentService.getFees(eq(commonHeader.getRequestId()), any(GecGetFeesRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        FeeCalculatorErrorCode.NO_FEE_FOUND));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                feeCalculatorService.getFee(commonHeader, getFeeRequest)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains(FeeCalculatorErrorCode.NO_FEE_FOUND));
    }

    @Test
    void testGetFee_UnexpectedException() {
        // Arrange
        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE))
                .thenReturn(pspConfiguration);
        when(basePaymentService.getFees(eq(commonHeader.getRequestId()), any(GecGetFeesRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                feeCalculatorService.getFee(commonHeader, getFeeRequest)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains(FeeCalculatorErrorCode.ERROR_RETRIEVING_FEES));
    }

    @Test
    void testGetFee_MultipleTransfers() {
        // Arrange - notice with multiple transfers
        Transfer transfer1 = new Transfer();
        transfer1.setPaTaxCode(PA_TAX_CODE);
        transfer1.setCategory("TAX");

        Transfer transfer2 = new Transfer();
        transfer2.setPaTaxCode("88888888888");
        transfer2.setCategory("BONUS");

        Notice notice = new Notice();
        notice.setPaTaxCode(PA_TAX_CODE);
        notice.setNoticeNumber(NOTICE_NUMBER);
        notice.setAmount(AMOUNT);
        notice.setTransfers(List.of(transfer1, transfer2));

        getFeeRequest.setNotices(List.of(notice));

        when(basePaymentService.retrievePSPConfiguration(ACQUIRER_ID, NodeApi.FEE))
                .thenReturn(pspConfiguration);
        when(basePaymentService.getFees(eq(commonHeader.getRequestId()), any(GecGetFeesRequest.class)))
                .thenReturn(getFeeResponse);

        // Act
        ResponseEntity<GetFeeResponse> response = feeCalculatorService.getFee(commonHeader, getFeeRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<GecGetFeesRequest> captorGecRequest = ArgumentCaptor.forClass(GecGetFeesRequest.class);
        verify(basePaymentService).getFees(eq(commonHeader.getRequestId()), captorGecRequest.capture());

        assertEquals(2, captorGecRequest.getValue().getTransferList().size());
        assertEquals(PA_TAX_CODE, captorGecRequest.getValue().getTransferList().get(0).getCreditorInstitution());
        assertEquals("TAX", captorGecRequest.getValue().getTransferList().get(0).getTransferCategory());
        assertEquals("88888888888", captorGecRequest.getValue().getTransferList().get(1).getCreditorInstitution());
        assertEquals("BONUS", captorGecRequest.getValue().getTransferList().get(1).getTransferCategory());
    }
}

