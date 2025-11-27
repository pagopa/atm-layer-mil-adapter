package it.gov.pagopa.miladapter.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ADAccessToken {
    @JsonProperty("token_type")
    private String type;

    @JsonProperty("expires_on")
    private long expiresOn;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("access_token")
    private String token;
}
