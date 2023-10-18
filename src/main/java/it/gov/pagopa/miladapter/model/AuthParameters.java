package it.gov.pagopa.miladapter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AuthParameters {

    private String requestId;
    private String terminalId;
    private String channel;
    private String acquirerId;
}
