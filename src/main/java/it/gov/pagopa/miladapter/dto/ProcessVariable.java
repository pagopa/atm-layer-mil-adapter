package it.gov.pagopa.miladapter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessVariable {

    private Object value;

    private String type;

}
