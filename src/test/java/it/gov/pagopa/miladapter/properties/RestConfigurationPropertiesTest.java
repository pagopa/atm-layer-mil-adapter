package it.gov.pagopa.miladapter.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(RestConfigurationProperties.class)
@TestPropertySource(properties = {
        "rest-configuration.mil-base-path=https://example.com",
        "rest-configuration.interceptor-logging-enabled=true",
        "rest-configuration.connection-response-timeout-milliseconds=3000",
        "rest-configuration.connection-request-timeout-milliseconds=5000",
        "rest-configuration.max-retry=1",
        "rest-configuration.retry-interval-milliseconds=500",
        "rest-configuration.auth.mil-auth-path=path",
        "rest-configuration.auth.client-id=client-id",
        "rest-configuration.auth.client-secret=client-secret",
        "rest-configuration.auth.grant-type=client-credentials"
})
@ComponentScan(basePackageClasses = RestConfigurationProperties.class)
public class RestConfigurationPropertiesTest {

    @Autowired
    private RestConfigurationProperties restConfigurationProperties;

    @Test
    public void testRestConfigurationProperties() {
        assertEquals("https://example.com", restConfigurationProperties.getMilBasePath());
        assertTrue(restConfigurationProperties.isInterceptorLoggingEnabled());
        assertEquals(5000, restConfigurationProperties.getConnectionRequestTimeoutMilliseconds());
        assertEquals(3000, restConfigurationProperties.getConnectionResponseTimeoutMilliseconds());
        assertEquals(1, restConfigurationProperties.getMaxRetry());
        assertEquals(500, restConfigurationProperties.getRetryIntervalMilliseconds());
        assertFalse(restConfigurationProperties.isLogEngineInputVariablesEnabled());
        assertEquals(restConfigurationProperties.getAuth().getMilAuthPath(), "path");
        assertEquals(restConfigurationProperties.getAuth().getClientId(), "client-id");
        assertEquals(restConfigurationProperties.getAuth().getClientSecret(), "client-secret");
        assertEquals(restConfigurationProperties.getAuth().getGrantType(), "client-credentials");
    }
}
