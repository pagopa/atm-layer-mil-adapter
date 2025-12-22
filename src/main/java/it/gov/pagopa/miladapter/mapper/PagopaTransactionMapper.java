package it.gov.pagopa.miladapter.mapper;

import it.gov.pagopa.miladapter.services.dto.PagopaTransactionsDto;
import it.gov.pagopa.miladapter.services.model.PagoPaTransactionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = {Instant.class, ChronoUnit.class})
public abstract class PagopaTransactionMapper {

    /**
     * The expiration time of the payment token passed to the node
     */
    @Value("${paymentnotice.activatepayment.expiration-time}")
    BigInteger paymentNoticeExpirationTime;

    @Mapping(target = "reported", constant = "false")
    @Mapping(target = "billId", expression = "java(generateBillId(request.getBillAccountId()))")
    @Mapping(target = "tokenExpDt", expression = "java(generateTokenExpDt(request.getPayDate(), this.paymentNoticeExpirationTime))")
    @Mapping(target = "payOptDuedate", expression = "java(parsePayOptDuedate(request.getPayOptDuedate()))")
    @Mapping(target = "payDate", expression = "java(parsePayDate(request.getPayDate()))")
    public abstract PagopaTransactionsDto toDto(PagoPaTransactionRequest request);

    /**
     * Generates the bill ID from the bill account ID according to specific rules
     *
     * @param billAccountId the bill account ID
     * @return the generated bill ID
     */
    String generateBillId(String billAccountId) {
    String billId = billAccountId;

    if ((billAccountId != null) && (billAccountId.length() == 18)) {
      if (billAccountId.charAt(0) == '0') {
        billId = billAccountId.substring(3);
      } else if (billAccountId.charAt(0) == '1') {
        billId = billAccountId.substring(1);
      } else if (billAccountId.charAt(0) == '2') {
        billId = billAccountId.substring(1, 16);
      } else if (billAccountId.charAt(0) == '3') {
        billId = billAccountId.substring(1);
      }
    }
        return billId;
    }

    /**
     * Parses the payment option due date from a string to an Instant
     *
     * @param payOptDuedate the payment option due date as a string
     * @return the payment option due date as an Instant
     */
    Instant parsePayOptDuedate(String payOptDuedate) {
        if (payOptDuedate == null || payOptDuedate.isBlank()) {
            return null;
        }
        return LocalDate.parse(payOptDuedate)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }

    /**
     * Parses the payment date from a string to an Instant
     *
     * @param payDate the payment date as a string
     * @return the payment date as an Instant
     */
    Instant parsePayDate(String payDate) {
        if (payDate == null || payDate.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(payDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                .toInstant(ZoneOffset.UTC);
    }

    /**
     * Generates the token expiration date based on the payment date and the configured expiration time
     *
     * @param payDate        the payment date as a string
     * @param expirationTime the expiration time in milliseconds
     * @return the token expiration date as an Instant
     */
    Instant generateTokenExpDt(String payDate, BigInteger expirationTime) {
        Instant payDtInstant = parsePayDate(payDate);
        if (payDtInstant == null || expirationTime == null) {
            return null;
        }
        return payDtInstant.plus(expirationTime.longValue(), ChronoUnit.MILLIS);
    }

}
