package it.gov.pagopa.miladapter.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ParentSpanContext {
    @JsonProperty("traceId")
    private String traceId;
    @JsonProperty("spanId")
    private String spanId;

    public boolean isNotNull() {
        return StringUtils.isNotBlank(this.traceId) && StringUtils.isNotBlank(this.spanId);
    }

    public boolean isNull() {
        return !isNotNull();
    }
}
