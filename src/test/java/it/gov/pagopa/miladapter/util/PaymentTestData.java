package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.services.model.*;

import java.math.BigDecimal;
import java.util.UUID;

public final class PaymentTestData {

    public static final String ACQUIRER_ID = "4585625";
    public static final String PA_TAX_CODE = "77777777777";
    public static final String NOTICE_NUMBER = "302051234567890124";
    public static final String CHANNEL = "ATM";
    public static final BigDecimal AMOUNT = BigDecimal.valueOf(9999);
    public static final String IDEMPOTENCY_KEY = "key-123456";
    public static final String TRANSACTION_ID = "550e8400e29b41d4a716446655440000";
    public static final String PAYMENT_TOKEN = "a3b4c5d6e7f8g9h0";
    public static final String TERMINAL_ID = "0aB9wXyZ";

    public static CommonHeader getCommonHeader() {
        CommonHeader commonHeader = new CommonHeader();
        commonHeader.setAcquirerId(ACQUIRER_ID);
        commonHeader.setChannel(CHANNEL);
        commonHeader.setTerminalId("12345678");
        commonHeader.setRequestId("d0d654e6-97da-4848-b568-99fedccb642b");
        return commonHeader;
    }

    public static PspConfiguration getPspConfiguration() {
        PspConfiguration pspConfiguration = new PspConfiguration();
        pspConfiguration.setPsp("AGID_01");
        pspConfiguration.setBroker("97735020584");
        pspConfiguration.setChannel("97735020584_07");
        pspConfiguration.setPassword("PLACEHOLDER");
        return pspConfiguration;
    }

    public static ActivatePaymentNoticeRequest getActivatePaymentRequest() {
        ActivatePaymentNoticeRequest activatePaymentNoticeRequest = new ActivatePaymentNoticeRequest();
        activatePaymentNoticeRequest.setIdempotencyKey(IDEMPOTENCY_KEY);
        activatePaymentNoticeRequest.setAmount(AMOUNT);
        return activatePaymentNoticeRequest;
    }

    public static ClosePaymentRequest getClosePaymentRequest(boolean isOk) {
        ClosePaymentRequest closePaymentRequest = new ClosePaymentRequest();
        closePaymentRequest.setOutcome(isOk ? PaymentTransactionOutcome.CLOSE.name() :
                PaymentTransactionOutcome.ERROR_ON_PAYMENT.name());
        closePaymentRequest.setPaymentMethod(PaymentMethod.PAGOBANCOMAT.name());
        closePaymentRequest.setPaymentTimestamp("2022-11-12T08:53:55");
        return closePaymentRequest;
    }

    /**
     * Example taken from <a href="https://docs.pagopa.it/avviso-pagamento/struttura/specifiche-tecniche/dati-per-il-pagamento/codice-qr">QR Code specification</a>
     */
    public static final String QR_CODE = "PAGOPA|002|"+NOTICE_NUMBER+"|"+PA_TAX_CODE+"|"+AMOUNT;

    // ACQUIRER ID
    public static final String ACQUIRER_ID_KNOWN = "4585625";
    public static final String ACQUIRER_ID_NOT_KNOWN = "4585626";

    public static PagoPaTransactionRequest getPagoPaTransactionRequest() {
        return new PagoPaTransactionRequest(
                TRANSACTION_ID,
                "C", // status: Completed
                "012345678901234567", // billAccountId (18 chars starting with 0)
                AMOUNT,
                ACQUIRER_ID,
                "IT60X0542811101000000123456", // billerIban
                new BigDecimal("1.50"), // billerCommission
                new BigDecimal("2.00"), // bankCommission
                IDEMPOTENCY_KEY,
                PA_TAX_CODE,
                NOTICE_NUMBER,
                "0000", // retCode: success
                "0", // outcomeCode: OK
                "Payment for notice " + NOTICE_NUMBER,
                "Test Biller Name",
                "Test Biller Office",
                new BigDecimal("10.00"), // payOptAmount
                "OPT", // payOptType
                "Optional payment note",
                "2024-12-31", // payOptDuedate
                PAYMENT_TOKEN,
                "crdRef_" + UUID.randomUUID().toString().substring(0, 8),
                TERMINAL_ID,
                "2024-01-15T10:30:00" // payDate
        );
    }

    private PaymentTestData() {
    }


}
