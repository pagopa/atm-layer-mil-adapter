package it.gov.pagopa.miladapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequiredProcessVariables {
    REQUEST_ID(null, "RequestId", null),
    ACQUIRER_ID("AcquirerId", "AcquirerId", "acquirerId"),
    CHANNEL("Channel", "Channel", null),
    TERMINAL_ID("TerminalId", "TerminalId", "terminalId"),
    CLIENT_ID(null, "client_id", null),
    CLIENT_SECRET(null, "client_secret", null),
    GRANT_TYPE(null, "grant_type", null),
    BRANCH_ID("branchId", null, "branchId"),
    FUNCTION_ID("functionId", null, "functionType"),
    CODE("code", null, "code"),
    ACTIVITY_PARENT_SPAN("activityParentSpan", null, null),
    TRANSACTION_ID("transactionId", "TransactionId", null),
    IDPAY_KEY("Ocp-Apim-Subscription-Key",null,null),
    FLOW("flow", null, null);

    private String engineValue;
    private String authenticatorValue;
    private String modelValue;

}
