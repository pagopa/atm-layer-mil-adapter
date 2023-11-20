package it.gov.pagopa.miladapter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TerminalParameters {

    private String terminalId;
    private String channel;
    private String acquirerId;
    private String branchId;
    private String code;
}
