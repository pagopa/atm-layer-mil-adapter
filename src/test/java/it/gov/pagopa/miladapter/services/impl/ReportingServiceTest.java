package it.gov.pagopa.miladapter.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.JFixture;
import it.gov.pagopa.miladapter.mapper.PagopaTransactionMapper;
import it.gov.pagopa.miladapter.mapper.PagopaTransferMapper;
import it.gov.pagopa.miladapter.services.dto.ErrorResponse;
import it.gov.pagopa.miladapter.services.dto.PagopaTransactionsDto;
import it.gov.pagopa.miladapter.services.dto.PagopaTransferListDto;
import it.gov.pagopa.miladapter.services.exception.ReportingServiceException;
import it.gov.pagopa.miladapter.services.model.PagoPaTransactionRequest;
import it.gov.pagopa.miladapter.services.model.PagoPaTransferListRequest;
import it.gov.pagopa.miladapter.util.PaymentTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PagopaTransactionMapper transactionMapper;

    @Mock
    private PagopaTransferMapper transferMapper;

    @InjectMocks
    private ReportingService reportingService;

    private JFixture fixture;
    private PagoPaTransactionRequest transactionRequest;
    private PagopaTransactionsDto mappedTransactionDto;
    private PagoPaTransferListRequest transferListRequest;
    private List<PagopaTransferListDto> mappedTransferList;
    private String baseUrl;
    private String transactionPath;
    private String transferPath;

    @BeforeEach
    void setUp() {
        fixture = new JFixture();
        fixture.customise().circularDependencyBehaviour().omitSpecimen();

        baseUrl = "http://reporting-service:8080";
        transactionPath = "/api/v1/reporting-service/transactions";
        transferPath = "/api/v1/reporting-service/transfers";

        ReflectionTestUtils.setField(reportingService, "reportingServiceBaseUrl", baseUrl);
        ReflectionTestUtils.setField(reportingService, "pagopaTransactionsPath", transactionPath);
        ReflectionTestUtils.setField(reportingService, "pagoPaTransferList", transferPath);
        ReflectionTestUtils.setField(reportingService, "failOnPartialError", true);

        transactionRequest = PaymentTestData.getPagoPaTransactionRequest();
        mappedTransactionDto = fixture.create(PagopaTransactionsDto.class);

        transferListRequest = fixture.create(PagoPaTransferListRequest.class);
        mappedTransferList = createMappedTransferList();

        lenient().when(transactionMapper.toDto(transactionRequest)).thenReturn(mappedTransactionDto);
        lenient().when(transferMapper.toDtoList(transferListRequest)).thenReturn(mappedTransferList);
    }

    private List<PagopaTransferListDto> createMappedTransferList() {
        List<PagopaTransferListDto> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PagopaTransferListDto dto = fixture.create(PagopaTransferListDto.class);
            dto.setTransactionId(12345);
            dto.setTransferId(i + 1);
            list.add(dto);
        }
        return list;
    }

    // Transaction Tests

    @Test
    void testCreateTransaction_Success() {
        // Arrange
        PagopaTransactionsDto responseDto = fixture.create(PagopaTransactionsDto.class);
        ResponseEntity<PagopaTransactionsDto> expectedResponse =
            new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        when(restTemplate.postForEntity(
            (baseUrl + transactionPath),
            (mappedTransactionDto),
            (PagopaTransactionsDto.class)
        )).thenReturn(expectedResponse);

        // Act
        ResponseEntity<PagopaTransactionsDto> result = reportingService.createTransaction(transactionRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(responseDto, result.getBody());
        verify(transactionMapper).toDto(transactionRequest);
        verify(restTemplate).postForEntity(
            (baseUrl + transactionPath),
            (mappedTransactionDto),
            (PagopaTransactionsDto.class)
        );
    }

    @Test
    void testCreateTransaction_SuccessWithOkStatus() {
        // Arrange
        PagopaTransactionsDto responseDto = fixture.create(PagopaTransactionsDto.class);
        ResponseEntity<PagopaTransactionsDto> expectedResponse =
            new ResponseEntity<>(responseDto, HttpStatus.OK);

        when(restTemplate.postForEntity(
            (baseUrl + transactionPath),
            (mappedTransactionDto),
            (PagopaTransactionsDto.class)
        )).thenReturn(expectedResponse);

        // Act
        ResponseEntity<PagopaTransactionsDto> result = reportingService.createTransaction(transactionRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responseDto, result.getBody());
    }

    @Test
    void testCreateTransaction_UnexpectedStatusCode() {
        // Arrange
        ResponseEntity<PagopaTransactionsDto> errorResponse =
            new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(
            (baseUrl + transactionPath),
            (mappedTransactionDto),
            (PagopaTransactionsDto.class)
        )).thenReturn(errorResponse);

        // Act & Assert
        ReportingServiceException exception = assertThrows(
            ReportingServiceException.class,
            () -> reportingService.createTransaction(transactionRequest)
        );

        assertTrue(exception.getMessage().contains("Unexpected response status"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    void testCreateTransaction_BadRequest_WithParsableErrorResponse() throws JsonProcessingException {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Validation error: field cannot be null");
        String errorBody = "{\"message\":\"Validation error: field cannot be null\"}";

        HttpClientErrorException.BadRequest badRequestException =
                (HttpClientErrorException.BadRequest) HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    null,
                    errorBody.getBytes(),
                    null
                );

        when(restTemplate.postForEntity(
            (baseUrl + transactionPath),
            (mappedTransactionDto),
            (PagopaTransactionsDto.class)
        )).thenThrow(badRequestException);

        // Act & Assert
        ReportingServiceException exception = assertThrows(
            ReportingServiceException.class,
            () -> reportingService.createTransaction(transactionRequest)
        );

        assertTrue(exception.getMessage().contains("Remote service returned 400"));
        assertTrue(exception.getMessage().contains("Validation error: field cannot be null"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    // Transfer List Tests

    @Test
    void testCreateTransferList_AllSuccess() {
        // Arrange
        ResponseEntity<PagopaTransferListDto> successResponse =
            new ResponseEntity<>(fixture.create(PagopaTransferListDto.class), HttpStatus.CREATED);

        when(restTemplate.postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        )).thenReturn(successResponse);

        // Act
        ResponseEntity<String> result = reportingService.createTransferList(transferListRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().contains("Success: 3"));
        assertTrue(result.getBody().contains("Failed: 0"));
        verify(transferMapper).toDtoList(transferListRequest);
        verify(restTemplate, times(3)).postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        );
    }

    @Test
    void testCreateTransferList_PartialSuccess_FailOnPartialErrorTrue() {
        // Arrange
        ReflectionTestUtils.setField(reportingService, "failOnPartialError", true);

        ResponseEntity<PagopaTransferListDto> successResponse =
            new ResponseEntity<>(fixture.create(PagopaTransferListDto.class), HttpStatus.CREATED);

        String errorBody = "{\"message\":\"Validation error\"}";
        HttpClientErrorException.BadRequest badRequestException =
                (HttpClientErrorException.BadRequest) HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    null,
                    errorBody.getBytes(),
                    null
                );

        when(restTemplate.postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        ))
            .thenReturn(successResponse)
            .thenThrow(badRequestException);

        // Act & Assert
        assertThrows(
            ReportingServiceException.class,
            () -> reportingService.createTransferList(transferListRequest)
        );

        verify(restTemplate, times(2)).postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        );
    }

    @Test
    void testCreateTransferList_PartialSuccess_FailOnPartialErrorFalse() throws JsonProcessingException {
        // Arrange
        ReflectionTestUtils.setField(reportingService, "failOnPartialError", false);

        ResponseEntity<PagopaTransferListDto> successResponse =
            new ResponseEntity<>(fixture.create(PagopaTransferListDto.class), HttpStatus.CREATED);

        String errorBody = "{\"message\":\"Validation error\"}";
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Validation error");

        HttpClientErrorException.BadRequest badRequestException =
                (HttpClientErrorException.BadRequest) HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    null,
                    errorBody.getBytes(),
                    null
                );


        when(restTemplate.postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        ))
            .thenReturn(successResponse)
            .thenThrow(badRequestException)
            .thenReturn(successResponse);

        // Act
        ResponseEntity<String> result = reportingService.createTransferList(transferListRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().contains("Success: 2"));
        assertTrue(result.getBody().contains("Failed: 1"));
        verify(restTemplate, times(3)).postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        );
    }

    @Test
    void testCreateTransferList_AllFailed_FailOnPartialErrorFalse() throws JsonProcessingException {
        // Arrange
        ReflectionTestUtils.setField(reportingService, "failOnPartialError", false);

        String errorBody = "{\"message\":\"Validation error\"}";
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Validation error");

        HttpClientErrorException.BadRequest badRequestException =
                (HttpClientErrorException.BadRequest) HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    null,
                    errorBody.getBytes(),
                    null
                );

        when(restTemplate.postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        )).thenThrow(badRequestException);

        // Act
        ResponseEntity<String> result = reportingService.createTransferList(transferListRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().contains("Success: 0"));
        assertTrue(result.getBody().contains("Failed: 3"));
        verify(restTemplate, times(3)).postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        );
    }

    @Test
    void testCreateTransferList_EmptyList() {
        // Arrange
        when(transferMapper.toDtoList(transferListRequest)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<String> result = reportingService.createTransferList(transferListRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().contains("Success: 0"));
        assertTrue(result.getBody().contains("Failed: 0"));
        verify(restTemplate, never()).postForEntity(
            anyString(),
            any(),
            any()
        );
    }

    @Test
    void testCreateTransferList_PartialErrorThrowsException() throws JsonProcessingException {
        // Arrange
        ReflectionTestUtils.setField(reportingService, "failOnPartialError", true);

        ResponseEntity<PagopaTransferListDto> successResponse =
            new ResponseEntity<>(fixture.create(PagopaTransferListDto.class), HttpStatus.CREATED);

        String errorBody = "{\"message\":\"Database constraint violation\"}";
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Database constraint violation");

        HttpClientErrorException.BadRequest badRequestException =
                (HttpClientErrorException.BadRequest) HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    null,
                    errorBody.getBytes(),
                    null
                );

        when(restTemplate.postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        ))
            .thenReturn(successResponse)
            .thenReturn(successResponse)
            .thenThrow(badRequestException);

        // Act & Assert
        ReportingServiceException exception = assertThrows(
            ReportingServiceException.class,
            () -> reportingService.createTransferList(transferListRequest)
        );

        assertTrue(exception.getMessage().contains("Database constraint violation"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testCreateTransferList_UnexpectedStatusCode() {
        // Arrange
        ResponseEntity<PagopaTransferListDto> errorResponse =
            new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        )).thenReturn(errorResponse);

        // Act & Assert
        assertThrows(
            ReportingServiceException.class,
            () -> reportingService.createTransferList(transferListRequest)
        );
    }

    // URL Construction Tests

    @Test
    void testCreateTransaction_VerifyUrlConstruction() {
        // Arrange
        ResponseEntity<PagopaTransactionsDto> expectedResponse =
            new ResponseEntity<>(mappedTransactionDto, HttpStatus.CREATED);

        when(restTemplate.postForEntity(
            anyString(),
            any(),
            eq(PagopaTransactionsDto.class)
        )).thenReturn(expectedResponse);

        // Act
        reportingService.createTransaction(transactionRequest);

        // Assert
        verify(restTemplate).postForEntity(
            (baseUrl + transactionPath),
            (mappedTransactionDto),
            (PagopaTransactionsDto.class)
        );
    }

    @Test
    void testCreateTransferList_VerifyUrlConstruction() {
        // Arrange
        ResponseEntity<PagopaTransferListDto> successResponse =
            new ResponseEntity<>(fixture.create(PagopaTransferListDto.class), HttpStatus.CREATED);

        when(restTemplate.postForEntity(
            anyString(),
            any(),
            eq(PagopaTransferListDto.class)
        )).thenReturn(successResponse);

        // Act
        reportingService.createTransferList(transferListRequest);

        // Assert
        verify(restTemplate, times(3)).postForEntity(
            eq(baseUrl + transferPath),
            any(PagopaTransferListDto.class),
            eq(PagopaTransferListDto.class)
        );
    }
}
