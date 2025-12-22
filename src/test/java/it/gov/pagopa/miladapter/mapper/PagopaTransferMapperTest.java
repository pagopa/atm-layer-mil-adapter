package it.gov.pagopa.miladapter.mapper;

import com.flextrade.jfixture.JFixture;
import it.gov.pagopa.miladapter.services.dto.PagopaTransferListDto;
import it.gov.pagopa.miladapter.services.model.PagoPaTransferListRequest;
import it.gov.pagopa.miladapter.services.model.Transfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {PagopaTransferMapperImpl.class})
class PagopaTransferMapperTest {

    @Autowired
    private PagopaTransferMapper mapper;

    private JFixture fixture;

    @BeforeEach
    void setUp() {
        fixture = new JFixture();
        fixture.customise().circularDependencyBehaviour().omitSpecimen();
    }

    @Test
    void testToDto_Success_AllFieldsPopulated() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(12345));
        request.setPagopaReported(false);

        Transfer transfer = new Transfer();
        transfer.setIdTransfer(1);
        transfer.setTransferAmount(BigInteger.valueOf(10000)); // 100.00 euro in cents
        transfer.setPaTaxCode("12345678901");
        transfer.setCompany("Test Company SPA");
        transfer.setIban("IT60X0542811101000000123456");
        transfer.setRemittanceInformation("Payment for invoice #123");
        transfer.setCategory("0101101IM");
        
        PagopaTransferListDto result = mapper.toDto(request, transfer);

        assertNotNull(result);
        assertEquals(request.getTransactionId().intValue(), result.getTransactionId());
        assertEquals(transfer.getIdTransfer(), result.getTransferId());
        assertEquals(new BigDecimal(transfer.getTransferAmount()), result.getTransferAmount());
        assertEquals(request.getPagopaReported(), result.getPagopaReported());
        assertEquals(LocalDate.now(), result.getTransferExecutionDt());
        assertEquals(transfer.getPaTaxCode(), result.getPaFiscalCode());
        assertEquals(transfer.getCompany(), result.getPaName());
        assertEquals(transfer.getIban(), result.getPaIban());
        assertEquals(transfer.getRemittanceInformation(), result.getRmtInfo());
    }

    @Test
    void testToDto_WithNullOptionalFields() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(12345));
        request.setPagopaReported(true);

        Transfer transfer = new Transfer();
        transfer.setIdTransfer(2);
        transfer.setTransferAmount(BigInteger.valueOf(5000));
        transfer.setPaTaxCode("98765432109");
        transfer.setCompany(null); // Optional field
        transfer.setIban("IT45T0300203280486870136629");
        transfer.setRemittanceInformation("Payment reference");
        transfer.setCategory("0101101IM");

        PagopaTransferListDto result = mapper.toDto(request, transfer);

        assertNotNull(result);
        assertEquals(request.getTransactionId().intValue(), result.getTransactionId());
        assertEquals(transfer.getIdTransfer(), result.getTransferId());
        assertNull(result.getPaName());
        assertEquals(transfer.getPaTaxCode(), result.getPaFiscalCode());
        assertEquals(transfer.getIban(), result.getPaIban());
        assertEquals(transfer.getRemittanceInformation(), result.getRmtInfo());
        assertTrue(result.getPagopaReported());
    }

    @Test
    void testToDto_WithZeroTransferAmount() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(99999));
        request.setPagopaReported(false);

        Transfer transfer = new Transfer();
        transfer.setIdTransfer(3);
        transfer.setTransferAmount(BigInteger.ZERO);
        transfer.setPaTaxCode("11111111111");
        transfer.setCompany("Zero Amount Company");
        transfer.setIban("IT28W8000000292100645211111");
        transfer.setRemittanceInformation("Zero amount transfer");
        transfer.setCategory("0101101IM");

        PagopaTransferListDto result = mapper.toDto(request, transfer);
        
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTransferAmount());
    }

    @Test
    void testToDto_VerifyTransferExecutionDateIsToday() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(12345));
        request.setPagopaReported(false);

        Transfer transfer = new Transfer();
        transfer.setIdTransfer(1);
        transfer.setTransferAmount(BigInteger.valueOf(10000));
        transfer.setPaTaxCode("12345678901");
        transfer.setIban("IT60X0542811101000000123456");
        transfer.setRemittanceInformation("Test");
        transfer.setCategory("0101101IM");

        PagopaTransferListDto result = mapper.toDto(request, transfer);
        
        assertNotNull(result.getTransferExecutionDt());
        assertEquals(LocalDate.now(), result.getTransferExecutionDt());
    }

    @Test
    void testToDtoList_Success_MultipleTransfers() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(54321));
        request.setPagopaReported(false);

        List<Transfer> transfers = new ArrayList<>();
        
        Transfer transfer1 = new Transfer();
        transfer1.setIdTransfer(1);
        transfer1.setTransferAmount(BigInteger.valueOf(5000));
        transfer1.setPaTaxCode("12345678901");
        transfer1.setCompany("Company One");
        transfer1.setIban("IT60X0542811101000000123456");
        transfer1.setRemittanceInformation("Payment 1");
        transfer1.setCategory("0101101IM");
        transfers.add(transfer1);

        Transfer transfer2 = new Transfer();
        transfer2.setIdTransfer(2);
        transfer2.setTransferAmount(BigInteger.valueOf(3000));
        transfer2.setPaTaxCode("98765432109");
        transfer2.setCompany("Company Two");
        transfer2.setIban("IT45T0300203280486870136629");
        transfer2.setRemittanceInformation("Payment 2");
        transfer2.setCategory("0101101IM");
        transfers.add(transfer2);

        Transfer transfer3 = new Transfer();
        transfer3.setIdTransfer(3);
        transfer3.setTransferAmount(BigInteger.valueOf(2000));
        transfer3.setPaTaxCode("11111111111");
        transfer3.setCompany("Company Three");
        transfer3.setIban("IT28W8000000292100645211111");
        transfer3.setRemittanceInformation("Payment 3");
        transfer3.setCategory("0101101IM");
        transfers.add(transfer3);

        request.setTransfers(transfers);

        List<PagopaTransferListDto> result = mapper.toDtoList(request);
        
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify first transfer
        PagopaTransferListDto dto1 = result.getFirst();
        assertEquals(54321, dto1.getTransactionId());
        assertEquals(1, dto1.getTransferId());
        assertEquals(new BigDecimal("5000"), dto1.getTransferAmount());
        assertEquals("12345678901", dto1.getPaFiscalCode());
        assertEquals("Company One", dto1.getPaName());
        assertEquals("IT60X0542811101000000123456", dto1.getPaIban());
        assertEquals("Payment 1", dto1.getRmtInfo());
        assertFalse(dto1.getPagopaReported());

        // Verify second transfer
        PagopaTransferListDto dto2 = result.get(1);
        assertEquals(2, dto2.getTransferId());
        assertEquals(new BigDecimal("3000"), dto2.getTransferAmount());
        assertEquals("98765432109", dto2.getPaFiscalCode());

        // Verify third transfer
        PagopaTransferListDto dto3 = result.get(2);
        assertEquals(3, dto3.getTransferId());
        assertEquals(new BigDecimal("2000"), dto3.getTransferAmount());
        assertEquals("11111111111", dto3.getPaFiscalCode());
    }

    @Test
    void testToDtoList_EmptyTransferList() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(12345));
        request.setTransfers(new ArrayList<>());
        request.setPagopaReported(false);

        List<PagopaTransferListDto> result = mapper.toDtoList(request);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToDtoList_NullRequest() {
        List<PagopaTransferListDto> result = mapper.toDtoList(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToDtoList_NullTransfersList() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(12345));
        request.setTransfers(null);
        request.setPagopaReported(false);
        
        List<PagopaTransferListDto> result = mapper.toDtoList(request);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToDtoList_SingleTransfer() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(99999));
        request.setPagopaReported(true);

        Transfer transfer = new Transfer();
        transfer.setIdTransfer(1);
        transfer.setTransferAmount(BigInteger.valueOf(15000));
        transfer.setPaTaxCode("12345678901");
        transfer.setCompany("Single Transfer Company");
        transfer.setIban("IT60X0542811101000000123456");
        transfer.setRemittanceInformation("Single payment");
        transfer.setCategory("0101101IM");

        request.setTransfers(Collections.singletonList(transfer));
        
        List<PagopaTransferListDto> result = mapper.toDtoList(request);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(99999, result.getFirst().getTransactionId());
        assertEquals(1, result.getFirst().getTransferId());
        assertTrue(result.getFirst().getPagopaReported());
    }

    @Test
    void testToDtoList_PagopaReportedTrue() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(11111));
        request.setPagopaReported(true);

        List<Transfer> transfers = new ArrayList<>();
        
        Transfer transfer1 = new Transfer();
        transfer1.setIdTransfer(1);
        transfer1.setTransferAmount(BigInteger.valueOf(1000));
        transfer1.setPaTaxCode("12345678901");
        transfer1.setIban("IT60X0542811101000000123456");
        transfer1.setRemittanceInformation("Reported payment 1");
        transfer1.setCategory("0101101IM");
        transfers.add(transfer1);

        Transfer transfer2 = new Transfer();
        transfer2.setIdTransfer(2);
        transfer2.setTransferAmount(BigInteger.valueOf(2000));
        transfer2.setPaTaxCode("98765432109");
        transfer2.setIban("IT45T0300203280486870136629");
        transfer2.setRemittanceInformation("Reported payment 2");
        transfer2.setCategory("0101101IM");
        transfers.add(transfer2);

        request.setTransfers(transfers);

        List<PagopaTransferListDto> result = mapper.toDtoList(request);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getPagopaReported());
        assertTrue(result.get(1).getPagopaReported());
    }

    @Test
    void testToDto_LargeTransferAmount() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(12345));
        request.setPagopaReported(false);

        Transfer transfer = new Transfer();
        transfer.setIdTransfer(1);
        transfer.setTransferAmount(new BigInteger("999999999999")); // Very large amount
        transfer.setPaTaxCode("12345678901");
        transfer.setCompany("Large Transfer Company");
        transfer.setIban("IT60X0542811101000000123456");
        transfer.setRemittanceInformation("Large payment");
        transfer.setCategory("0101101IM");

        PagopaTransferListDto result = mapper.toDto(request, transfer);
        
        assertNotNull(result);
        assertEquals(new BigDecimal("999999999999"), result.getTransferAmount());
    }

    @Test
    void testToDto_MaxLengthStrings() {
        PagoPaTransferListRequest request = new PagoPaTransferListRequest();
        request.setTransactionId(BigInteger.valueOf(12345));
        request.setPagopaReported(false);

        String maxLengthCompany = "A".repeat(140); // Max 140 chars for paName
        String maxLengthRmtInfo = "B".repeat(140); // Max 140 chars for rmtInfo
        String validIban = "IT60X0542811101000000123456"; // Valid IBAN (max 34 chars)

        Transfer transfer = new Transfer();
        transfer.setIdTransfer(1);
        transfer.setTransferAmount(BigInteger.valueOf(10000));
        transfer.setPaTaxCode("12345678901");
        transfer.setCompany(maxLengthCompany);
        transfer.setIban(validIban);
        transfer.setRemittanceInformation(maxLengthRmtInfo);
        transfer.setCategory("0101101IM");

        PagopaTransferListDto result = mapper.toDto(request, transfer);
        
        assertNotNull(result);
        assertEquals(140, result.getPaName().length());
        assertEquals(140, result.getRmtInfo().length());
        assertEquals(maxLengthCompany, result.getPaName());
        assertEquals(maxLengthRmtInfo, result.getRmtInfo());
    }
}

