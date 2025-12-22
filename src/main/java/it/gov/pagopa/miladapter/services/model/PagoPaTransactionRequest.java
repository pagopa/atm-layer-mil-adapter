package it.gov.pagopa.miladapter.services.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoPaTransactionRequest {
    private String transactionId;
    private String status;
    private String billAccountId;
    private BigDecimal billAmount;
    private String senderBank;
    private String billerIban;
    private BigDecimal billerCommission;
    private BigDecimal bankCommission;
    private String idempotencyKey;
    private String billerFiscalCode;
    private String noticeNumber;
    private String retCode;
    private String outcomeCode;
    private String payDescription;
    private String billerName;
    private String billerOffice;
    private BigDecimal payOptAmount;
    private String payOptType;
    private String payOptNote;
    private String payOptDuedate;
    private String payToken;
    private String crdReferenceId;
    private String atmCode;
    private String payDate;
}
