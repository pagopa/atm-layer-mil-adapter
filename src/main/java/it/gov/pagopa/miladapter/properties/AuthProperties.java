package it.gov.pagopa.miladapter.properties;

import lombok.Data;

@Data
public class AuthProperties {
    private String milAuthPath;
    private String clientId;
    private String clientSecret;
    private String grantType;
    private Boolean enableAuth;
}
