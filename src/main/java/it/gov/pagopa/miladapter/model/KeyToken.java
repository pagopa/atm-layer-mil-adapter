package it.gov.pagopa.miladapter.model;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class KeyToken {

    private String terminalId;

    private String acquirerId;

    private String channel;

    @Override
    public String toString() {
        return this.channel.concat("_").concat(acquirerId).concat("_").concat(terminalId);
    }
}
