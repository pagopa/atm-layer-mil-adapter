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

    private String transactionId;

    @Override
    public String toString() {
        StringBuilder keyTokenBuilder = new StringBuilder();

        appendIfNotNull(keyTokenBuilder, acquirerId);
        appendIfNotNull(keyTokenBuilder, channel);
        appendIfNotNull(keyTokenBuilder, terminalId);
        appendIfNotNull(keyTokenBuilder, transactionId);

        return keyTokenBuilder.toString();
    }

    private void appendIfNotNull(StringBuilder builder, String value) {
        if (value != null && !value.isEmpty()) {
            if (builder.length() > 0) {
                builder.append("_");
            }
            builder.append(value);
        }
    }
}
