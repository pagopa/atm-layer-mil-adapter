package it.gov.pagopa.miladapter.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CamundaWaitMessage {

    private String messageName;

    private String processInstanceId;

    private Map<String, ProcessVariable> processVariables;

}