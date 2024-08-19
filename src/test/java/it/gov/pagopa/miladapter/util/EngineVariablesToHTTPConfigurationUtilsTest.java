package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.AuthProperties;
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
        variables.put("millAccessToken", "VALID_TOKEN");
        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "transaction-id");
        variables.put(HttpVariablesEnum.URL.getValue(), "http://prova");
        variables.put(HttpVariablesEnum.METHOD.getValue(), "GET");

        Map<String, Object> headers = new CaseInsensitiveMap<>();
        headers.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "bank_id");
        headers.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "term_id");
        headers.put(RequiredProcessVariables.CHANNEL.getEngineValue(), "ATM");

        variables.put(HttpVariablesEnum.HEADERS.getValue(), headers);

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCall(variables, true, false);
        assertEquals("http://prova", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertNull(configuration.getBody());
        assertEquals(6, configuration.getHeaders().size());
        Assertions.assertTrue(configuration.getHeaders().containsKey(RequiredProcessVariables.REQUEST_ID.getAuthenticatorValue()));
        assertEquals(new HashMap<>(), configuration.getPathParams());

    }

//    @Test
//    void getHttpConfigurationWithPathParamsTest() {
//        Map<String, Object> variables = new CaseInsensitiveMap<>();
//        variables.put("millAccessToken", "VALID_TOKEN");
//        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "transaction-id");
//        variables.put(HttpVariablesEnum.URL.getValue(), "http://prova");
//        variables.put(HttpVariablesEnum.METHOD.getValue(), "GET");
//
//        Map<String, Object> headers = new CaseInsensitiveMap<>();
//        headers.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "bank_id");
//        headers.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "term_id");
//        headers.put(RequiredProcessVariables.CHANNEL.getEngineValue(), "ATM");
//
//        variables.put(HttpVariablesEnum.HEADERS.getValue(), headers);
//
//        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
//                .getHttpConfigurationExternalCall(variables, true, false);
//        assertEquals("http://prova", configuration.getEndpoint());
//        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
//        assertNull(configuration.getBody());
//        assertEquals(6, configuration.getHeaders().size());
//        Assertions.assertTrue(configuration.getHeaders().containsKey(RequiredProcessVariables.REQUEST_ID.getAuthenticatorValue()));
//        assertEquals(0, configuration.getPathParams().size());
//        assertEquals("1", configuration.getPathParams().get("id"));
//    }

    @Test
    void getHttpConfigurationExternalCallNewTestIDPAY() {
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
                .getHttpConfigurationExternalCall(variables, true, false);

        assertEquals("http://prova", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertEquals("testBody", configuration.getBody());
        assertEquals(6, configuration.getHeaders().size());
        assertNotNull(configuration.getHeaders().get("RequestId"));

        assertEquals("transaction-id", ((List<?>)configuration.getHeaders().get("transactionId")).get(0));
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
                .getHttpConfigurationExternalCall(variables,false,true);


        assertEquals("http://prova", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertEquals("testBody", configuration.getBody());
        assertEquals(7, configuration.getHeaders().size());
        assertNotNull(configuration.getHeaders().get("RequestId"));

        assertEquals("transaction-id", (configuration.getHeaders().get("transactionId")).get(0));
        assertEquals("Bearer VALID_TOKEN", (configuration.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0));

        assertEquals(new HashMap<>(), configuration.getPathParams());
        assertNotNull(configuration.getAuthParameters());
        assertEquals("bank_id", configuration.getAuthParameters().getAcquirerId());
        assertEquals("ATM", configuration.getAuthParameters().getChannel());
        assertEquals("term_id", configuration.getAuthParameters().getTerminalId());
        assertEquals("transaction-id", configuration.getAuthParameters().getTransactionId());
    }


    @Test
    void getHttpConfigurationExternalCallNewTest_PartialDeclaredHeaderParam() {
        Map<String, Object> variables = new HashMap<>();
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("AcquirerId", "bank_id");
        headersMap.put("Channel", "channel");
        headersMap.put("TerminalId", "term_id");
        variables.put("headers", headersMap);
        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "transaction-id");
        variables.put("url", "http://prova");
        variables.put("method", "GET");
        variables.put("millAccessToken", "VALID_TOKEN");
        variables.put("body", "testBody");

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCall(variables, false, true);

        assertEquals("http://prova", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertEquals("testBody", configuration.getBody());
        assertEquals(7, configuration.getHeaders().size());
        assertNotNull(configuration.getHeaders().get("RequestId"));

        assertEquals("transaction-id", ((List<?>)configuration.getHeaders().get("transactionId")).get(0));
        assertEquals("Bearer VALID_TOKEN", ((List<?>)configuration.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0));

        assertEquals(new HashMap<>(), configuration.getPathParams());
        assertNotNull(configuration.getAuthParameters());
        assertEquals("bank_id", configuration.getAuthParameters().getAcquirerId());
        assertEquals("channel", configuration.getAuthParameters().getChannel());
        assertEquals("term_id", configuration.getAuthParameters().getTerminalId());
        assertEquals("transaction-id", configuration.getAuthParameters().getTransactionId());
    }

    @Test
    void getHttpConfigurationGenerateTokenCallTest() {
        Map<String, Object> variables = new CaseInsensitiveMap<>();
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("AcquirerId", "bank_id");
        headersMap.put("Channel", "ATM");
        headersMap.put("TerminalId", "term_id");
        variables.put("headers", headersMap);
        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "transaction-id");
        variables.put("url", "http://prova");
        variables.put("millAccessToken", "VALID_TOKEN");
        variables.put("PathParams", new HashMap<>());

        AuthProperties authProperties = new AuthProperties();
        authProperties.setClientId("s");
        authProperties.setGrantType("s");
        authProperties.setMilAuthPath("s");
        authProperties.setClientSecret("s");

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationGenerateTokenCall(variables,authProperties);

        assertEquals(HttpMethod.POST, configuration.getHttpMethod());
        assertEquals("grant_type=s&client_secret=s&client_id=s", configuration.getBody());
        assertEquals(4, configuration.getHeaders().size());
        assertNotNull(configuration.getHeaders().get("RequestId"));
    }
}
