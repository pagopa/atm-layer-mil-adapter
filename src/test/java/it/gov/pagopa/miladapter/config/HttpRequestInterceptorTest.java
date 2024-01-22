package it.gov.pagopa.miladapter.config;

import io.opentelemetry.api.OpenTelemetry;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HttpRequestInterceptor.class})
public class HttpRequestInterceptorTest {

    @MockBean
    OpenTelemetry openTelemetry;

    @MockBean
    RestConfigurationProperties restConfigurationProperties;

    @InjectMocks
    private HttpRequestInterceptor httpRequestInterceptor;

    @Test
    public void testIntercept() throws IOException {
        httpRequestInterceptor = new HttpRequestInterceptor(openTelemetry,restConfigurationProperties);
        MockClientHttpRequest request = new MockClientHttpRequest();
        byte[] body = "body".getBytes(StandardCharsets.UTF_8);
        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        MockClientHttpResponse mockClientHttpResponse = new MockClientHttpResponse();
        when(clientHttpRequestExecution.execute(any(), any())).thenReturn(mockClientHttpResponse);
        when(restConfigurationProperties.isInterceptorLoggingEnabled()).thenReturn(true);

        assertSame(mockClientHttpResponse, httpRequestInterceptor.intercept(request, body, clientHttpRequestExecution));
        verify(clientHttpRequestExecution).execute(any(), any());
    }
}
