package it.gov.pagopa.miladapter.properties;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gec")
@Data
public class GecProperties {

    private Touchpoint touchpoint;
    private PaymentMethod paymentmethod;

    @Data
    public static class Touchpoint {
        private Map<String, String> map;
    }

    @Data
    public static class PaymentMethod {
        private Map<String, String> map;
    }
}
