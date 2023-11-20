package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EngineVariablesToHTTPConfigurationUtilsTest {
    @Mock
    HttpRequestUtils httpRequestUtils;

    @Test
    public void getHttpConfigurationEmptyPathParamsTest() {

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
        assertEquals(null, configuration.getBody());
        assertEquals(1, configuration.getHeaders().size());
        assertTrue(configuration.getHeaders().containsKey(RequiredProcessVariables.REQUEST_ID.getMilValue()));
        assertEquals(new MultivaluedHashMap(), configuration.getPathParams());

    }

    @Test
    public void getHttpConfigurationWithPathParamsTest() {
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
        assertEquals(null, configuration.getBody());
        assertEquals(1, configuration.getHeaders().size());
        assertTrue(configuration.getHeaders().containsKey(RequiredProcessVariables.REQUEST_ID.getMilValue()));
        assertEquals(1, configuration.getPathParams().size());
        assertTrue(configuration.getPathParams().containsKey("id"));
        assertEquals("1", configuration.getPathParams().get("id"));
    }

    @Test
    public void getHttpConfigurationInternalCallWithPathParamsTest() {
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
    public void defaultConstructorTest() {
        Object engineVariablesToHTTPConfigurationUtils = new EngineVariablesToHTTPConfigurationUtils();
        assertTrue(engineVariablesToHTTPConfigurationUtils instanceof EngineVariablesToHTTPConfigurationUtils);
    }

}
