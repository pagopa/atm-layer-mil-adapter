package it.gov.pagopa.miladapter.model;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
