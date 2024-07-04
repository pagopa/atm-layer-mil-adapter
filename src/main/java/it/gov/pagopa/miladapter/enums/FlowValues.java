package it.gov.pagopa.miladapter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FlowValues {
    MIL("MIL"),
    IDPAY("IDPAY");

    private String value;
}
