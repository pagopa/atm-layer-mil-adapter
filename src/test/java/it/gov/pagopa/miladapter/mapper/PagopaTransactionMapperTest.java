package it.gov.pagopa.miladapter.mapper;

import com.flextrade.jfixture.JFixture;
import it.gov.pagopa.miladapter.services.dto.PagopaTransactionsDto;
import it.gov.pagopa.miladapter.services.model.PagoPaTransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {PagopaTransactionMapperImpl.class})
@ActiveProfiles("test")
class PagopaTransactionMapperTest {

    @Autowired
    private PagopaTransactionMapper mapper;

    private JFixture fixture;

    @BeforeEach
    void setUp() {
        fixture = new JFixture();
        fixture.customise().circularDependencyBehaviour().omitSpecimen();

        // Set the expiration time for the mapper (5 minutes in milliseconds)
        ReflectionTestUtils.setField(mapper, "paymentNoticeExpirationTime", BigInteger.valueOf(300000L));
    }

    @Test
    void testToDto_Success() {
        // Arrange
        PagoPaTransactionRequest request = fixture.create(PagoPaTransactionRequest.class);
        request.setBillAccountId("012345678901234567"); // 18 characters starting with 0
        request.setPayDate("2024-01-15T10:30:00");
        request.setPayOptDuedate("2024-12-31");

        // Act
        PagopaTransactionsDto result = mapper.toDto(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.getTransactionId(), result.getTransactionId());
        assertEquals(request.getStatus(), result.getStatus());
        assertEquals("345678901234567", result.getBillAccountId());
        assertEquals(request.getBillAmount(), result.getBillAmount());
        assertEquals(request.getSenderBank(), result.getSenderBank());
        assertEquals(request.getBillerIban(), result.getBillerIban());
        assertEquals(request.getBillerCommission(), result.getBillerCommission());
        assertEquals(request.getBankCommission(), result.getBankCommission());
        assertEquals(request.getIdempotencyKey(), result.getIdempotencyKey());
        assertEquals(request.getBillerFiscalCode(), result.getBillerFiscalCode());
        assertEquals(request.getNoticeNumber(), result.getNoticeNumber());
        assertEquals(request.getRetCode(), result.getRetCode());
        assertEquals(request.getOutcomeCode(), result.getOutcomeCode());
        assertEquals(request.getPayDescription(), result.getPayDescription());
        assertEquals(request.getBillerName(), result.getBillerName());
        assertEquals(request.getBillerOffice(), result.getBillerOffice());
        assertEquals(request.getPayOptAmount(), result.getPayOptAmount());
        assertEquals(request.getPayOptType(), result.getPayOptType());
        assertEquals(request.getPayOptNote(), result.getPayOptNote());
        assertEquals(request.getPayToken(), result.getPayToken());
        assertEquals(request.getCrdReferenceId(), result.getCrdReferenceId());
        assertEquals(request.getAtmCode(), result.getAtmCode());
        assertFalse(result.getReported());
        assertNotNull(result.getBillId());
        assertNotNull(result.getPayDate());
        assertNotNull(result.getPayOptDuedate());
        assertNotNull(result.getTokenExpDt());
    }

    @ParameterizedTest
    @CsvSource({
        "012345678901234567, 345678901234567",  // starts with 0
        "112345678901234567, 12345678901234567", // starts with 1
        "212345678901234567, 123456789012345",   // starts with 2
        "312345678901234567, 12345678901234567", // starts with 3
        "412345678901234567, 412345678901234567", // starts with 4
        "12345, 12345"                           // less than 18 chars
    })
    void testGenerateBillId(String billAccountId, String expectedBillId) {
        // Act
        String result = mapper.generateBillId(billAccountId);

        // Assert
        assertEquals(expectedBillId, result);
    }

    @Test
    void testGenerateBillId_Null() {
        // Act
        String result = mapper.generateBillId(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testParsePayOptDuedate_Success() {
        // Arrange
        String payOptDuedate = "2024-12-31";
        Instant expected = LocalDate.parse(payOptDuedate)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        // Act
        Instant result = mapper.parsePayOptDuedate(payOptDuedate);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    void testParsePayOptDuedate_Null() {
        // Act
        Instant result = mapper.parsePayOptDuedate(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testParsePayOptDuedate_Blank() {
        // Act
        Instant result = mapper.parsePayOptDuedate("   ");

        // Assert
        assertNull(result);
    }

    @Test
    void testParsePayDate_Success() {
        // Arrange
        String payDate = "2024-01-15T10:30:00";
        Instant expected = LocalDateTime.parse(payDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                .toInstant(ZoneOffset.UTC);

        // Act
        Instant result = mapper.parsePayDate(payDate);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    void testParsePayDate_Null() {
        // Act
        Instant result = mapper.parsePayDate(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testParsePayDate_Blank() {
        // Act
        Instant result = mapper.parsePayDate("   ");

        // Assert
        assertNull(result);
    }

    @Test
    void testGenerateTokenExpDt_Success() {
        // Arrange
        String payDate = "2024-01-15T10:30:00";
        BigInteger expirationTime = BigInteger.valueOf(300000L); // 5 minutes

        Instant payDateInstant = LocalDateTime.parse(payDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                .toInstant(ZoneOffset.UTC);
        Instant expected = payDateInstant.plus(expirationTime.longValue(), ChronoUnit.MILLIS);

        // Act
        Instant result = mapper.generateTokenExpDt(payDate, expirationTime);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    void testGenerateTokenExpDt_NullPayDate() {
        // Arrange
        BigInteger expirationTime = BigInteger.valueOf(300000L);

        // Act
        Instant result = mapper.generateTokenExpDt(null, expirationTime);

        // Assert
        assertNull(result);
    }

    @Test
    void testGenerateTokenExpDt_NullExpirationTime() {
        // Arrange
        String payDate = "2024-01-15T10:30:00";

        // Act
        Instant result = mapper.generateTokenExpDt(payDate, null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToDto_WithNullOptionalFields() {
        // Arrange
        PagoPaTransactionRequest request = new PagoPaTransactionRequest(
                "txn123",
                "C",
                "billAcc123",
                new BigDecimal("100.00"),
                "12345",
                "IT60X0542811101000000123456",
                new BigDecimal("1.50"),
                new BigDecimal("2.00"),
                "idempKey123",
                "12345678901",
                "123456789012345678",
                "0000",
                "0",
                "Payment description",
                "Biller Name",
                "Biller Office",
                null, // payOptAmount
                null, // payOptType
                null, // payOptNote
                null, // payOptDuedate
                "payToken123",
                "crdRef123",
                "ATM001",
                "2024-01-15T10:30:00"
        );

        // Act
        PagopaTransactionsDto result = mapper.toDto(request);

        // Assert
        assertNotNull(result);
        assertNull(result.getPayOptAmount());
        assertNull(result.getPayOptType());
        assertNull(result.getPayOptNote());
        assertNull(result.getPayOptDuedate());
        assertFalse(result.getReported());
    }

    @Test
    void testToDto_WithAllFieldsPopulated() {
        // Arrange
        PagoPaTransactionRequest request = new PagoPaTransactionRequest(
                "txn123",
                "C",
                "012345678901234567",
                new BigDecimal("100.00"),
                "12345",
                "IT60X0542811101000000123456",
                new BigDecimal("1.50"),
                new BigDecimal("2.00"),
                "idempKey123",
                "12345678901",
                "123456789012345678",
                "0000",
                "0",
                "Payment description",
                "Biller Name",
                "Biller Office",
                new BigDecimal("10.00"),
                "OPT",
                "Optional note",
                "2024-12-31",
                "payToken123",
                "crdRef123",
                "ATM001",
                "2024-01-15T10:30:00"
        );

        // Act
        PagopaTransactionsDto result = mapper.toDto(request);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.getPayOptAmount());
        assertEquals("OPT", result.getPayOptType());
        assertEquals("Optional note", result.getPayOptNote());
        assertNotNull(result.getPayOptDuedate());
        assertEquals("345678901234567", result.getBillId());
        assertNotNull(result.getTokenExpDt());
        assertTrue(result.getTokenExpDt().isAfter(result.getPayDate()));
    }
}
