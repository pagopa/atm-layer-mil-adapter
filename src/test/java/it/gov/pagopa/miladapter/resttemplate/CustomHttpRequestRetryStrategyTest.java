package it.gov.pagopa.miladapter.resttemplate;

import it.gov.pagopa.miladapter.services.MILRestService;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class CustomHttpRequestRetryStrategyTest {

    @Mock
    private HttpResponse httpResponseMocked;

    @Mock
    private HttpContext httpContextMocked;

    @BeforeEach
    public void setup() {
        // before every test create a mock
        MockitoAnnotations.initMocks(this);
    }

    private Integer maxRetry = 2;
    private Integer retryInterval = 500;

   // @Test void retryRequestTest () {
     //
   // }

}
