package it.gov.pagopa.miladapter.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.JFixture;
import it.gov.pagopa.miladapter.mapper.PagopaTransactionMapper;
import it.gov.pagopa.miladapter.services.dto.ErrorResponse;
import it.gov.pagopa.miladapter.services.dto.PagopaTransactionsDto;
import it.gov.pagopa.miladapter.services.model.PagoPaTransactionRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PagopaTransactionMapper mapper;

    @InjectMocks
    private ReportingService reportingService;

    private JFixture fixture;
    private PagoPaTransactionRequest request;
    private PagopaTransactionsDto mappedDto;
    private String baseUrl;
    private String path;

    @BeforeEach
    void setUp() {
        fixture = new JFixture();
        fixture.customise().circularDependencyBehaviour().omitSpecimen();

        baseUrl = "http://reporting-service:8080";
        path = "/api/v1/reporting-service/transactions";

        ReflectionTestUtils.setField(reportingService, "reportingServiceBaseUrl", baseUrl);
        ReflectionTestUtils.setField(reportingService, "pagopaTransactionsPath", path);

        request = fixture.create(PagoPaTransactionRequest.class);
        mappedDto = fixture.create(PagopaTransactionsDto.class);

        when(mapper.toDto(request)).thenReturn(mappedDto);
    }

    @Test
    void testCreateTransaction_Success() {
        // Arrange
        PagopaTransactionsDto responseDto = fixture.create(PagopaTransactionsDto.class);
        ResponseEntity<PagopaTransactionsDto> expectedResponse =
            new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        when(restTemplate.postForEntity(
            (baseUrl + path),
            (mappedDto),
            (PagopaTransactionsDto.class)
        )).thenReturn(expectedResponse);

        // Act
        ResponseEntity<PagopaTransactionsDto> result = reportingService.createTransaction(request);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(responseDto, result.getBody());
        verify(mapper).toDto(request);
        verify(restTemplate).postForEntity(
            (baseUrl + path),
            (mappedDto),
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
            (baseUrl + path),
            (mappedDto),
            (PagopaTransactionsDto.class)
        )).thenReturn(expectedResponse);

        // Act
        ResponseEntity<PagopaTransactionsDto> result = reportingService.createTransaction(request);

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
            (baseUrl + path),
            (mappedDto),
            (PagopaTransactionsDto.class)
        )).thenReturn(errorResponse);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> reportingService.createTransaction(request)
        );

        assertTrue(exception.getMessage().contains("Unexpected response status"));
        assertTrue(exception.getMessage().contains("500"));
    }

    @Test
    void testCreateTransaction_VerifyUrlConstruction() {
        // Arrange
        ResponseEntity<PagopaTransactionsDto> expectedResponse =
            new ResponseEntity<>(mappedDto, HttpStatus.CREATED);

        when(restTemplate.postForEntity(
            anyString(),
            any(),
            eq(PagopaTransactionsDto.class)
        )).thenReturn(expectedResponse);

        // Act
        reportingService.createTransaction(request);

        // Assert
        verify(restTemplate).postForEntity(
            (baseUrl + path),
            (mappedDto),
            (PagopaTransactionsDto.class)
        );
    }

    @Test
    void testCreateTransaction_VerifyMapperInvocation() {
        // Arrange
        ResponseEntity<PagopaTransactionsDto> expectedResponse =
            new ResponseEntity<>(mappedDto, HttpStatus.OK);

        when(restTemplate.postForEntity(
            anyString(),
            any(),
            eq(PagopaTransactionsDto.class)
        )).thenReturn(expectedResponse);

        // Act
        reportingService.createTransaction(request);

        // Assert
        verify(mapper, times(1)).toDto(request);
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
            (baseUrl + path),
            (mappedDto),
            (PagopaTransactionsDto.class)
        )).thenThrow(badRequestException);

        when(objectMapper.readValue(errorBody, ErrorResponse.class)).thenReturn(errorResponse);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reportingService.createTransaction(request)
        );

        assertTrue(exception.getMessage().contains("Remote service returned 400"));
        assertTrue(exception.getMessage().contains("Validation error: field cannot be null"));
        verify(objectMapper).readValue(errorBody, ErrorResponse.class);
    }

    @Test
    void testCreateTransaction_BadRequest_WithUnparsableErrorResponse() throws JsonProcessingException {
        // Arrange
        String malformedErrorBody = "This is not valid JSON";

        HttpClientErrorException.BadRequest badRequestException =
                (HttpClientErrorException.BadRequest) HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    null,
                    malformedErrorBody.getBytes(),
                    null
                );

        when(restTemplate.postForEntity(
            (baseUrl + path),
            (mappedDto),
            (PagopaTransactionsDto.class)
        )).thenThrow(badRequestException);

        when(objectMapper.readValue(malformedErrorBody, ErrorResponse.class))
            .thenThrow(new JsonProcessingException("Cannot parse JSON") {});

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reportingService.createTransaction(request)
        );

        assertTrue(exception.getMessage().contains("Remote service returned 400"));
        assertTrue(exception.getMessage().contains("error body could not be parsed"));
        assertTrue(exception.getMessage().contains(malformedErrorBody));
        assertNotNull(exception.getCause());
        assertInstanceOf(JsonProcessingException.class, exception.getCause());
    }

    @Test
    void testCreateTransaction_BadRequest_WithEmptyErrorResponse() throws JsonProcessingException {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("");
        String errorBody = "{\"message\":\"\"}";

        HttpClientErrorException.BadRequest badRequestException =
                (HttpClientErrorException.BadRequest) HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    null,
                    errorBody.getBytes(),
                    null
                );

        when(restTemplate.postForEntity(
            (baseUrl + path),
            (mappedDto),
            (PagopaTransactionsDto.class)
        )).thenThrow(badRequestException);

        when(objectMapper.readValue(errorBody, ErrorResponse.class)).thenReturn(errorResponse);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reportingService.createTransaction(request)
        );

        assertTrue(exception.getMessage().contains("Remote service returned 400"));
    }

    @Test
    void testCreateTransaction_BadRequest_WithNullMessage() throws JsonProcessingException {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(null);
        String errorBody = "{\"message\":null}";

        HttpClientErrorException.BadRequest badRequestException =
                (HttpClientErrorException.BadRequest) HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    null,
                    errorBody.getBytes(),
                    null
                );

        when(restTemplate.postForEntity(
            (baseUrl + path),
            (mappedDto),
            (PagopaTransactionsDto.class)
        )).thenThrow(badRequestException);

        when(objectMapper.readValue(errorBody, ErrorResponse.class)).thenReturn(errorResponse);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reportingService.createTransaction(request)
        );

        assertTrue(exception.getMessage().contains("Remote service returned 400"));
        assertTrue(exception.getMessage().contains("null"));
    }
}
