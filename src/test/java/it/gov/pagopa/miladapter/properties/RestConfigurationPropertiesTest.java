package it.gov.pagopa.miladapter.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(RestConfigurationProperties.class)
@TestPropertySource(properties = {
        "rest-configuration.mil-authenticator-base-path=https://example.com",
        "rest-configuration.interceptor-logging-enabled=true",
        "rest-configuration.connection-response-timeout-milliseconds=3000",
        "rest-configuration.connection-request-timeout-milliseconds=5000",
        "rest-configuration.max-retry=1",
        "rest-configuration.retry-interval-milliseconds=500",
        "rest-configuration.auth.mil-authenticator-path=path",
        "rest-configuration.auth.client-id=client-id",
        "rest-configuration.auth.client-secret=client-secret",
        "rest-configuration.auth.grant-type=client-credentials"
})
@ComponentScan(basePackageClasses = RestConfigurationProperties.class)
class RestConfigurationPropertiesTest {

    @Autowired
    private RestConfigurationProperties restConfigurationProperties;

    @Test
    void testRestConfigurationProperties() {
        assertEquals("https://example.com", restConfigurationProperties.getMilAuthenticatorBasePath());
        assertTrue(restConfigurationProperties.isInterceptorLoggingEnabled());
        assertEquals(5000, restConfigurationProperties.getConnectionRequestTimeoutMilliseconds());
        assertEquals(3000, restConfigurationProperties.getConnectionResponseTimeoutMilliseconds());
        assertEquals(1, restConfigurationProperties.getMaxRetry());
        assertEquals(500, restConfigurationProperties.getRetryIntervalMilliseconds());
        assertFalse(restConfigurationProperties.isLogEngineInputVariablesEnabled());
        assertEquals("path", restConfigurationProperties.getAuth().getMilAuthenticatorPath());
        assertEquals("client-id", restConfigurationProperties.getAuth().getClientId());
        assertEquals("client-secret", restConfigurationProperties.getAuth().getClientSecret());
        assertEquals("client-credentials", restConfigurationProperties.getAuth().getGrantType());
    }
}
