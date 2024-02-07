package it.gov.pagopa.miladapter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "adapter-pool-configuration")
@Data
public class AdapterPoolConfigurationProperties {
    private PoolConfig rest;
    private PoolConfig completion;

    @Data
    public static class PoolConfig {
        private int corePoolSize;
        private int maxPoolSize;
        private String threadName;
        private String name;

    }
}
