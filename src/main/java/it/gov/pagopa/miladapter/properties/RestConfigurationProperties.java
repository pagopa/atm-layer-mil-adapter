package it.gov.pagopa.miladapter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest-configuration")
@Data
public class RestConfigurationProperties {

    private String milAuthenticatorBasePath;
    private String milBasePath;
    private String getTokenEndpoint;
    private String idPayBasePath;
    private boolean interceptorLoggingEnabled;
    private int connectionRequestTimeoutMilliseconds;
    private int connectionResponseTimeoutMilliseconds;
    private int maxRetry;
    private int retryIntervalMilliseconds;
    private boolean logEngineInputVariablesEnabled;
    private long asyncThreshold;
    private String modelBasePath;
    DefinitionIdProperties definitionIdProperties;
    AuthProperties auth;
}
