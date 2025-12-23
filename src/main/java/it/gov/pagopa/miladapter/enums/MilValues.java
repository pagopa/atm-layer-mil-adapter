package it.gov.pagopa.miladapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MilValues {
    IDEMPOTENCY_KEY("idempotencyKey"),
    AMOUNT("amount"),
    QRCODE("qrCode"),
    PA_TAX_CODE("paTaxCode"),
    NOTICE_NUMBER("noticeNumber"),
    TRANSACTION_ID("transactionId");

    private final String value;
}
