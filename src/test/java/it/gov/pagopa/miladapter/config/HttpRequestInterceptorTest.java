package it.gov.pagopa.miladapter.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.TextMapSetter;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HttpRequestInterceptor.class})
class HttpRequestInterceptorTest {

    @MockBean
    OpenTelemetry openTelemetry;

    @MockBean
    RestConfigurationProperties restConfigurationProperties;

    @InjectMocks
    private HttpRequestInterceptor httpRequestInterceptor;

    private HttpRequest httpRequest;
    private HttpHeaders httpHeaders;

    @Test
    void testSetter() {
        httpRequest = mock(HttpRequest.class);
        httpHeaders = new HttpHeaders();
        when(httpRequest.getHeaders()).thenReturn(httpHeaders);
        TextMapSetter<HttpRequest> setter = (request, key, value) -> request.getHeaders().add(key, value);
        setter.set(httpRequest, "Test-Key", "Test-Value");
        assert(httpHeaders.containsKey("Test-Key"));
        assert(httpHeaders.get("Test-Key").contains("Test-Value"));
    }

    @Test
    void testIntercept() throws IOException {
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
