package it.gov.pagopa.miladapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequiredProcessVariables {
    REQUEST_ID(null, "RequestId"),
    ACQUIRER_ID("bankId", "AcquirerId"),
    CHANNEL("channel", "Channel"),
    TERMINAL_ID("terminalId", "TerminalId"),
    CLIENT_ID(null, "client_id"),
    CLIENT_SECRET(null, "client_secret"),
    GRANT_TYPE(null, "grant_type");

    private String engineValue;
    private String milValue;

}
