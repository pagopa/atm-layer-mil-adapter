package it.gov.pagopa.miladapter.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class HttpRequestInterceptorTest {

    @InjectMocks
    private HttpRequestInterceptor httpRequestInterceptor;

    @Test
    void testIntercept() throws IOException {
        MockClientHttpRequest request = new MockClientHttpRequest();
        byte[] body = "body".getBytes(StandardCharsets.UTF_8);
        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        MockClientHttpResponse mockClientHttpResponse = new MockClientHttpResponse();
        when(clientHttpRequestExecution.execute(any(), any())).thenReturn(mockClientHttpResponse);
        assertSame(mockClientHttpResponse, httpRequestInterceptor.intercept(request, body, clientHttpRequestExecution));
        verify(clientHttpRequestExecution).execute(any(), any());
    }
}
