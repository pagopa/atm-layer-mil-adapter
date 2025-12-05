package it.gov.pagopa.miladapter.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class used when remapping faults from the node to mil outcomes
 */
@Configuration
@ConfigurationProperties(prefix = "node.error")
@Data
public class NodeErrorMappingProperties {
    private List<String> outcomes = new ArrayList<>();
    private Map<String, Integer> map = new HashMap<>();
}
