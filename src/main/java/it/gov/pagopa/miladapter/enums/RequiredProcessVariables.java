package it.gov.pagopa.miladapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequiredProcessVariables {
    REQUEST_ID(null, "RequestId", null),
    ACQUIRER_ID("bankId", "AcquirerId", "acquirerId"),
    CHANNEL("channel", "Channel", null),
    TERMINAL_ID("terminalId", "TerminalId", "terminalId"),
    CLIENT_ID(null, "client_id", null),
    CLIENT_SECRET(null, "client_secret", null),
    GRANT_TYPE(null, "grant_type", null),
    BRANCH_ID("branchId", null, "branchId"),
    FUNCTION_ID("functionId", null, "functionType"),
    CODE("code", null, "code"),
    ACTIVITY_PARENT_SPAN("activityParentSpan", null, null);

    private String engineValue;
    private String milValue;
    private String modelValue;

}
