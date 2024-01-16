package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void testParseIntegerWithValue() {
        Long value = 12345L;

        Integer result = EngineVariablesToHTTPConfigurationUtils.parseInteger(value);

        assertNotNull(result);
        assertEquals(value.intValue(), result);
    }

    @Test
    void testParseIntegerWithNullValue() {
        Integer result = null;

        assertNull(result);
    }

    @Test
    void getHttpConfigurationEmptyPathParamsTest() {

        Map<String, Object> variables = new CaseInsensitiveMap<>();
        variables.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "bank_id");
        variables.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "term_id");
        variables.put(RequiredProcessVariables.CHANNEL.getEngineValue(), "ATM");
        variables.put(HttpVariablesEnum.URL.getValue(), "http://prova");
        variables.put(HttpVariablesEnum.METHOD.getValue(), "GET");

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCall(variables, true);
        assertEquals("http://prova", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertNull(configuration.getBody());
        assertEquals(1, configuration.getHeaders().size());
        Assertions.assertTrue(configuration.getHeaders().containsKey(RequiredProcessVariables.REQUEST_ID.getAuthenticatorValue()));
        assertEquals(new MultivaluedHashMap<>(), configuration.getPathParams());

    }

    @Test
    void getHttpConfigurationWithPathParamsTest() {
        Map<String, Object> variables = new CaseInsensitiveMap<>();
        variables.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "bank_id");
        variables.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "term_id");
        variables.put(RequiredProcessVariables.CHANNEL.getEngineValue(), "ATM");
        variables.put(HttpVariablesEnum.URL.getValue(), "http://prova/{id}");
        variables.put(HttpVariablesEnum.METHOD.getValue(), "GET");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", "1");
        variables.put(HttpVariablesEnum.PATH_PARAMS.getValue(), pathParams);

        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCall(variables, true);
        assertEquals("http://prova/{id}", configuration.getEndpoint());
        assertEquals(HttpMethod.GET, configuration.getHttpMethod());
        assertNull(configuration.getBody());
        assertEquals(1, configuration.getHeaders().size());
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

}
