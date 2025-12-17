package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.services.model.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;

public final class PaymentTestData {

    public static final String ACQUIRER_ID = "4585625";
    public static final String PA_TAX_CODE = "77777777777";
    public static final String NOTICE_NUMBER = "302051234567890124";
    public static final String PSP_ID = "AGID_01";
    public static final String CHANNEL = "ATM";
    public static final BigDecimal AMOUNT = BigDecimal.valueOf(9999);
    public static final long FEE = 200;
    public static final String IDEMPOTENCY_KEY = "key-123456";
    public static final String TRANSACTION_ID = "550e8400e29b41d4a716446655440000";
    public static final String PAYMENT_TOKEN = "a3b4c5d6e7f8g9h0";
    public static final String TERMINAL_ID = "0aB9wXyZ";

    public static Map<String, String> getMilHeaders(boolean isPos, boolean isKnownAcquirer) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("RequestId", UUID.randomUUID().toString());
        headerMap.put("Version", "1.0.0");
        headerMap.put("AcquirerId", isKnownAcquirer ? PaymentTestData.ACQUIRER_ID_KNOWN : PaymentTestData.ACQUIRER_ID_NOT_KNOWN);
        headerMap.put("Channel", isPos ? "POS" : "ATM");
        headerMap.put("TerminalId", "0aB9wXyZ");
        if (isPos) headerMap.put("MerchantId", "28405fHfk73x88D");
        headerMap.put("SessionId", UUID.randomUUID().toString());
        return headerMap;
    }

    public static CommonHeader getCommonHeader() {
        CommonHeader commonHeader = new CommonHeader();
        commonHeader.setAcquirerId(ACQUIRER_ID);
        commonHeader.setChannel(CHANNEL);
        commonHeader.setTerminalId("12345678");
        commonHeader.setRequestId("d0d654e6-97da-4848-b568-99fedccb642b");
        return commonHeader;
    }

    public static HttpHeaders buildHttpHeaders(Map<String, String> headerMap) {
        HttpHeaders headers = new HttpHeaders();
        headerMap.forEach(headers::add);
        return headers;
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

    // CLOSE PAYMENT TRANSACTION ID
    public static final String PAY_TID_NODE_OK = "8b19db3262384cde9ced78cb0f059c5f";
    public static final String PAY_TID_NODE_KO = "27de01c5c4b24a48802a59696c2bef20";
    public static final String PAY_TID_NODE_400 = "0af4576bd3654abb83713ae84b32ce50";
    public static final String PAY_TID_NODE_404 = "724a08e550094699880498a71a65cd47";
    public static final String PAY_TID_NODE_408 = "50a4853f77694cfe91386961a3ff0646";
    public static final String PAY_TID_NODE_422 = "b1ec45e154fb48c494129f74d97ae66e";
    public static final String PAY_TID_NODE_500 = "519769bcafad45d8be9c569002499e96";
    public static final String PAY_TID_NODE_TIMEOUT = "968b64b284dc48a08eb948d8777bf9e6";
    public static final String PAY_TID_NODE_UNPARSABLE = "2720236097a54d2799a41671b0585747";


    private PaymentTestData() {
    }


}
