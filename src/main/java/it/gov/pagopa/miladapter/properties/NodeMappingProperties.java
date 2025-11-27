package it.gov.pagopa.miladapter.properties;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "node")
@Data
public class NodeMappingProperties {
    private Map<String, String> paymentMethod;
}
