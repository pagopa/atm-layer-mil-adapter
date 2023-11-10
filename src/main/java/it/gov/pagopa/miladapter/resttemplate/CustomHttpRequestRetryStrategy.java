package it.gov.pagopa.miladapter.resttemplate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.time.Duration;

@AllArgsConstructor
@Slf4j
public class CustomHttpRequestRetryStrategy implements HttpRequestRetryStrategy {

    private Integer maxRetry;
    private Integer retryInterval;


    @Override
    public boolean retryRequest(HttpRequest httpRequest, IOException e, int i, HttpContext httpContext) {
        boolean retryWillBeDone = i <= maxRetry;
        log.error("Failure on Rest call with exception, evaluating retry number {}, max retry attempts {}, retry will be done? : {}", i, maxRetry, retryWillBeDone, e);
        return retryWillBeDone;
    }

    @Override
    public boolean retryRequest(HttpResponse httpResponse, int i, HttpContext httpContext) {
        boolean statusOk = httpResponse.getCode() >= 200 && httpResponse.getCode() < 400;
        if (!statusOk) {
            boolean retryWillBeDone = i <= maxRetry;
            log.error("Failure on Rest call for statusCode, evaluating retry number {}, max retry attempts {}, retry will be done? : {}", i, maxRetry, retryWillBeDone);
            return retryWillBeDone;
        }
        return false;
    }

    @Override
    public TimeValue getRetryInterval(HttpRequest request, IOException exception, int execCount, HttpContext context) {
        log.error("retry will be done in {} milliseconds", retryInterval);
        return Timeout.of(Duration.ofMillis(retryInterval));
    }

    @Override
    public TimeValue getRetryInterval(HttpResponse httpResponse, int i, HttpContext httpContext) {
        log.error("retry will be done in {} milliseconds", retryInterval);
        return Timeout.of(Duration.ofMillis(retryInterval));
    }
}