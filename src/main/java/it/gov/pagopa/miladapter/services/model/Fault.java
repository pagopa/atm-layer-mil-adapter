package it.gov.pagopa.miladapter.services.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Fault {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String faultCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String faultString;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer serial;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String originalFaultCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String originalFaultString;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String originalDescription;
}
