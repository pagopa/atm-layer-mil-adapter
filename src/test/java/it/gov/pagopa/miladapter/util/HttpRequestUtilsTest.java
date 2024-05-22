package it.gov.pagopa.miladapter.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
class HttpRequestUtilsTest {
    @InjectMocks
    private HttpRequestUtils httpRequestUtils;

    @Test
    void testBuildHttpEntity() {
        String body = "{\"idempotencyKey\": \"71695110631_X935001531\",\n" + "10 \"amount\": 10000}";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Channel", "ATM");
        headers.add("AcquirerId", "06789");
        HttpEntity<String> httpEntity = HttpRequestUtils.buildHttpEntity(body, headers);
        assertEquals(body, httpEntity.getBody());
        assertEquals(headers, httpEntity.getHeaders());
    }

    @Test
    void testCreateHttpHeadersWithValidMap() {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Channel", "ATM");
        HttpHeaders headers = httpRequestUtils.createHttpHeaders(headersMap);
        assertEquals("ATM", headers.getFirst("Channel"));
    }

    @Test
    void testCreateHttpHeadersWithEmptyMap() {
        Map<String, String> headersMap = new HashMap<>();
        HttpHeaders headers = httpRequestUtils.createHttpHeaders(headersMap);
        Assertions.assertTrue(CollectionUtils.isEmpty(headers));
    }

    @Test
    void testBuildURI() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("paTaxCode", "00000000201");
        pathParams.put("noticeNumber", "012345678901234567");
        String basePath = "https://mil-d-apim.azure-api.net";
        String endpoint = "/endpoint/{paTaxCode}/{noticeNumber}";
        URI result = HttpRequestUtils.buildURI(basePath, endpoint, pathParams);
        assertEquals("https://mil-d-apim.azure-api.net/endpoint/00000000201/012345678901234567", result.toString());
    }

    @Test
    void testFromMapToHeaders() {
        Map<String, String> map = new HashMap<>();
        map.put("Channel", "");
        assertThrows(RuntimeException.class, () -> httpRequestUtils.fromMapToHeaders(map));
        map.clear();
        map.put("Channel", "ATM");
        HttpHeaders headersResponse = httpRequestUtils.fromMapToHeaders(map);
        assertTrue(Objects.requireNonNull(headersResponse.get("Channel")).equals(Arrays.asList("ATM")));
    }

    @Test
    void testCheckNotNullPathParamsException() {
        HashMap<String, String> map = new HashMap<>();
        map.put("Channel", "");
        assertThrows(RuntimeException.class, () -> HttpRequestUtils.checkNotNullPathParams(map));
    }

    @Test
    void testCheckNotNullPathParams() {
        HashMap<String, String> map = new HashMap<>();
        map.put("Channel", "ATM");
        assertDoesNotThrow(() -> HttpRequestUtils.checkNotNullPathParams(map));
    }

    @Test
    void testHttpMethodFromValueGet() {
        HttpMethod actualHttpMethodFromValueResult = HttpRequestUtils.httpMethodFromValue("GET");
        assertSame(actualHttpMethodFromValueResult.GET, actualHttpMethodFromValueResult);
    }

    @Test
    void testHttpMethodFromValueHead() {
        HttpMethod actualHttpMethodFromValueResult = HttpRequestUtils.httpMethodFromValue("HEAD");
        assertSame(actualHttpMethodFromValueResult.HEAD, actualHttpMethodFromValueResult);
    }

    @Test
    void testHttpMethodFromValuePost() {
        HttpMethod actualHttpMethodFromValueResult = HttpRequestUtils.httpMethodFromValue("POST");
        assertSame(actualHttpMethodFromValueResult.POST, actualHttpMethodFromValueResult);
    }

    @Test
    void testHttpMethodFromValuePut() {
        HttpMethod actualHttpMethodFromValueResult = HttpRequestUtils.httpMethodFromValue("PUT");
        assertSame(actualHttpMethodFromValueResult.PUT, actualHttpMethodFromValueResult);
    }

    @Test
    void testHttpMethodFromValuePatch() {
        HttpMethod actualHttpMethodFromValueResult = HttpRequestUtils.httpMethodFromValue("PATCH");
        assertSame(actualHttpMethodFromValueResult.PATCH, actualHttpMethodFromValueResult);
    }

    @Test
    void testHttpMethodFromValueDelete() {
        HttpMethod actualHttpMethodFromValueResult = HttpRequestUtils.httpMethodFromValue("DELETE");
        assertSame(actualHttpMethodFromValueResult.DELETE, actualHttpMethodFromValueResult);
    }

    @Test
    void testHttpMethodFromValueOptions() {
        HttpMethod actualHttpMethodFromValueResult = HttpRequestUtils.httpMethodFromValue("OPTIONS");
        assertSame(actualHttpMethodFromValueResult.OPTIONS, actualHttpMethodFromValueResult);
    }

    @Test
    void testHttpMethodFromValueTrace() {
        HttpMethod actualHttpMethodFromValueResult = HttpRequestUtils.httpMethodFromValue("TRACE");
        assertSame(actualHttpMethodFromValueResult.TRACE, actualHttpMethodFromValueResult);
    }

    @Test
    void testHttpMethodFromValueDefault() {
        assertThrows(RuntimeException.class, () -> HttpRequestUtils.httpMethodFromValue("Http method does not exist"));
    }

    @Test
    void defaultConstructorTest() {
        Object httpRequestUtilsConstructed = new HttpRequestUtils();
        assertTrue(httpRequestUtilsConstructed instanceof HttpRequestUtils);
    }
}

