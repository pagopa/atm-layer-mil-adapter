package it.gov.pagopa.miladapter.services.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagopaTransactionsDto {

    @NotNull
    @Size(max = 36)
    private String transactionId;

    @NotNull
    @Size(max = 1)
    private String status;

    @NotNull
    @Size(max = 36)
    private String billAccountId;

    @NotNull
    private BigDecimal billAmount;

    @NotNull
    @Size(max = 5)
    private String senderBank;

    @NotNull
    private Instant payDate;

    @NotNull
    private Boolean reported = false;

    @NotNull
    @Size(max = 27)
    private String billerIban;

    @NotNull
    @Size(max = 18)
    private String billId;

    @NotNull
    private BigDecimal billerCommission;

    @NotNull
    private BigDecimal bankCommission;

    @NotNull
    @Size(max = 22)
    private String idempotencyKey;

    @NotNull
    @Size(max = 11)
    private String billerFiscalCode;

    @NotNull
    @Size(max = 36)
    private String noticeNumber;

    @NotNull
    @Size(max = 4)
    private String retCode;

    @NotNull
    @Size(max = 1)
    private String outcomeCode;

    @NotNull
    @Size(max = 210)
    private String payDescription;

    @NotNull
    @Size(max = 140)
    private String billerName;

    @NotNull
    @Size(max = 140)
    private String billerOffice;

    private BigDecimal payOptAmount;

    @Size(max = 3)
    private String payOptType;

    private Instant payOptDuedate;

    @Size(max = 210)
    private String payOptNote;

    @NotNull
    @Size(max = 35)
    private String payToken;

    @NotNull
    private Instant tokenExpDt;

    @NotNull
    @Size(max = 35)
    private String crdReferenceId;

    @NotNull
    @Size(max = 10)
    private String atmCode;
}

