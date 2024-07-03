package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EngineVariablesToHTTPConfigurationUtilsTest {

    @Test
    void testGetIntegerValue_NullValue() {
        assertNull(EngineVariablesToHTTPConfigurationUtils.getIntegerValue("TestVariable", null));
    }

    @Test
    void testGetIntegerValue_EmptyValue() {
        assertThrows(RuntimeException.class, () -> EngineVariablesToHTTPConfigurationUtils.getIntegerValue("TestVariable", ""));
    }

    @Test
    void testGetIntegerValue_InvalidInteger() {
        assertThrows(RuntimeException.class, () -> EngineVariablesToHTTPConfigurationUtils.getIntegerValue("TestVariable", "abc"));
    }

    @Test
    void testGetIntegerValue_ValidInteger() {
        assertEquals(123, EngineVariablesToHTTPConfigurationUtils.getIntegerValue("TestVariable", "123"));
    }

    @Test
    void testParseIntegerWithNull() {
        assertNull(EngineVariablesToHTTPConfigurationUtils.parseInteger(null));
    }

    @Test
    void testParseIntegerWithLong() {
        Long input = 12345L;
        Integer expected = 12345;
        assertEquals(expected, EngineVariablesToHTTPConfigurationUtils.parseInteger(input));
    }

    @Test
    void testParseIntegerWithInteger() {
        Integer input = 12345;
        Integer expected = 12345;
        assertEquals(expected, EngineVariablesToHTTPConfigurationUtils.parseInteger(input));
    }

    @Test
    void testParseIntegerWithUnsupportedType() {
        Double input = 12345.0;
        assertThrows(IllegalArgumentException.class, () -> EngineVariablesToHTTPConfigurationUtils.parseInteger(input));
    }

    @Test
    void getHttpConfigurationEmptyPathParamsTest() {

        Map<String, Object> variables = new CaseInsensitiveMap<>();
        variables.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "bank_id");
        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "transaction-id");
        variables.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "term_id");
        variables.put(RequiredProcessVariables.CHANNEL.getEngineValue(), "ATM");
        variables.put(HttpVariablesEnum.URL.getValue(), "http://prova");
        variables.put(HttpVariablesEnum.METHOD.getValue(), "GET");
        variables.put("millAccessToken", "VALID_TOKEN");

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCall(variables, true, false);
        assertEquals("http://prova", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertNull(configuration.getBody());
        assertEquals(3, configuration.getHeaders().size());
        Assertions.assertTrue(configuration.getHeaders().containsKey(RequiredProcessVariables.REQUEST_ID.getAuthenticatorValue()));
        assertEquals(new HashMap<>(), configuration.getPathParams());

    }

    @Test
    void getHttpConfigurationWithPathParamsTest() {
        Map<String, Object> variables = new CaseInsensitiveMap<>();
        variables.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "bank_id");
        variables.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "term_id");
        variables.put(RequiredProcessVariables.CHANNEL.getEngineValue(), "ATM");
        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "transaction-id");
        variables.put(HttpVariablesEnum.URL.getValue(), "http://prova/{id}");
        variables.put(HttpVariablesEnum.METHOD.getValue(), "GET");
        variables.put("millAccessToken", "VALID_TOKEN");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", "1");
        variables.put(HttpVariablesEnum.PATH_PARAMS.getValue(), pathParams);

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCall(variables, true, false);
        assertEquals("http://prova/{id}", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertNull(configuration.getBody());
        assertEquals(3, configuration.getHeaders().size());
        Assertions.assertTrue(configuration.getHeaders().containsKey(RequiredProcessVariables.REQUEST_ID.getAuthenticatorValue()));
        assertEquals(1, configuration.getPathParams().size());
        Assertions.assertTrue(configuration.getPathParams().containsKey("id"));
        assertEquals("1", configuration.getPathParams().get("id"));
    }

    @Test
    void getHttpConfigurationInternalCallWithPathParamsTest() {
        Map<String, Object> variables = new CaseInsensitiveMap<>();
        variables.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "12345");
        variables.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "12345678");
        variables.put(RequiredProcessVariables.FUNCTION_ID.getEngineValue(), "FUNCTION_ID");
        variables.put(RequiredProcessVariables.CODE.getEngineValue(), "CODE");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", "1");
        variables.put(HttpVariablesEnum.PATH_PARAMS.getValue(), pathParams);

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationInternalCall(variables, true);
        assertEquals("FUNCTION_ID", configuration.getFunction());
        assertEquals("12345", configuration.getAuthParameters().getAcquirerId());
        assertEquals("12345678", configuration.getAuthParameters().getTerminalId());
        assertEquals("CODE", configuration.getAuthParameters().getCode());
    }

    @Test
    void getHttpConfigurationExternalCallNewTest() {
        Map<String, Object> variables = new CaseInsensitiveMap<>();
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("AcquirerId", "bank_id");
        headersMap.put("Channel", "ATM");
        headersMap.put("TerminalId", "term_id");
        variables.put("headers", headersMap);
        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "transaction-id");
        variables.put("url", "http://prova");
        variables.put("method", "GET");
        variables.put("millAccessToken", "VALID_TOKEN");
        variables.put("body", "testBody");
        variables.put("PathParams", new HashMap<>());

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCallNew(variables);

        assertEquals("http://prova", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertEquals("testBody", configuration.getBody());
        assertEquals(6, configuration.getHeaders().size());
        assertNotNull(configuration.getHeaders().get("RequestId"));

        assertEquals("transaction-id", ((List<?>)configuration.getHeaders().get("TransactionId")).get(0));
        assertEquals("Bearer VALID_TOKEN", ((List<?>)configuration.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0));

        assertEquals(new HashMap<>(), configuration.getPathParams());
        assertNotNull(configuration.getAuthParameters());
        assertEquals("bank_id", configuration.getAuthParameters().getAcquirerId());
        assertEquals("ATM", configuration.getAuthParameters().getChannel());
        assertEquals("term_id", configuration.getAuthParameters().getTerminalId());
        assertEquals("transaction-id", configuration.getAuthParameters().getTransactionId());
    }

    @Test
    void getHttpConfigurationExternalCallNewWithNullPathParamsTest() {
        Map<String, Object> variables = new CaseInsensitiveMap<>();
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("AcquirerId", "bank_id");
        headersMap.put("Channel", "ATM");
        headersMap.put("TerminalId", "term_id");
        variables.put("headers", headersMap);
        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "transaction-id");
        variables.put("url", "http://prova");
        variables.put("method", "GET");
        variables.put("millAccessToken", "VALID_TOKEN");
        variables.put("body", "testBody");

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCallNew(variables);


        assertEquals("http://prova", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertEquals("testBody", configuration.getBody());
        assertEquals(6, configuration.getHeaders().size());
        assertNotNull(configuration.getHeaders().get("RequestId"));

        assertEquals("transaction-id", ((List<?>)configuration.getHeaders().get("TransactionId")).get(0));
        assertEquals("Bearer VALID_TOKEN", ((List<?>)configuration.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0));

        assertEquals(new HashMap<>(), configuration.getPathParams());
        assertNotNull(configuration.getAuthParameters());
        assertEquals("bank_id", configuration.getAuthParameters().getAcquirerId());
        assertEquals("ATM", configuration.getAuthParameters().getChannel());
        assertEquals("term_id", configuration.getAuthParameters().getTerminalId());
        assertEquals("transaction-id", configuration.getAuthParameters().getTransactionId());
    }

    @Test
    void getHttpConfigurationExternalCallNewTest_NullHeaders() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("headers", null); // No headers provided
        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "transaction-id");
        variables.put("url", "http://prova");
        variables.put("method", "GET");
        variables.put("millAccessToken", "VALID_TOKEN");
        variables.put("body", "testBody");
        variables.put("PathParams", new HashMap<>());

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCallNew(variables);

        assertEquals("http://prova", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertEquals("testBody", configuration.getBody());
        assertEquals(6, configuration.getHeaders().size());
        assertNotNull(configuration.getHeaders().get("RequestId"));

        assertEquals("transaction-id", ((List<?>)configuration.getHeaders().get("TransactionId")).get(0));
        assertEquals("Bearer VALID_TOKEN", ((List<?>)configuration.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0));

        assertEquals(new HashMap<>(), configuration.getPathParams());
        assertNotNull(configuration.getAuthParameters());
        assertEquals(null, configuration.getAuthParameters().getAcquirerId());
        assertEquals(null, configuration.getAuthParameters().getChannel());
        assertEquals(null, configuration.getAuthParameters().getTerminalId());
        assertEquals("transaction-id", configuration.getAuthParameters().getTransactionId());
    }

    @Test
    void getHttpConfigurationExternalCallNewTest_PartialHeaders() {
        Map<String, Object> variables = new HashMap<>();
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("AcquirerId", "bank_id");
        headersMap.put("Channel", null); // Channel is null
        headersMap.put("TerminalId", "term_id");
        variables.put("headers", headersMap);
        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "transaction-id");
        variables.put("url", "http://prova");
        variables.put("method", "GET");
        variables.put("millAccessToken", "VALID_TOKEN");
        variables.put("body", "testBody");
        variables.put("PathParams", new HashMap<>());

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCallNew(variables);

        assertEquals("http://prova", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertEquals("testBody", configuration.getBody());
        assertEquals(6, configuration.getHeaders().size());
        assertNotNull(configuration.getHeaders().get("RequestId"));

        assertEquals("transaction-id", ((List<?>)configuration.getHeaders().get("TransactionId")).get(0));
        assertEquals("Bearer VALID_TOKEN", ((List<?>)configuration.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0));

        assertEquals(new HashMap<>(), configuration.getPathParams());
        assertNotNull(configuration.getAuthParameters());
        assertEquals("bank_id", configuration.getAuthParameters().getAcquirerId());
        assertEquals(null, configuration.getAuthParameters().getChannel()); // Channel should be null
        assertEquals("term_id", configuration.getAuthParameters().getTerminalId());
        assertEquals("transaction-id", configuration.getAuthParameters().getTransactionId());
    }
}
