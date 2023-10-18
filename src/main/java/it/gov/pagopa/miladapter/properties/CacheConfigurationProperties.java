package it.gov.pagopa.miladapter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache-configuration")
@Data
public class CacheConfigurationProperties {
    private String cacheName;
    private int maxEntries;
    private int tokenSecurityThreshold;
}
