package it.gov.pagopa.miladapter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Token implements Serializable {

    private String access_token;

    private String token_type;

    private int expires_in;

    private long time;

}
