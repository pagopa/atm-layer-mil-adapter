package it.gov.pagopa.miladapter.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = false)
public class AuthParameters extends TerminalParameters {

    private String requestId;
    private String transactionId;
}
