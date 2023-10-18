package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.model.HTTPConfiguration;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class EngineVariablesToHTTPConfigurationUtilsTest {
    @Mock
    HttpRequestUtils httpRequestUtils;

    @Test
    public void getHttpConfigurationEmptyPathParamsTest() {
        try (MockedStatic<EngineVariablesUtils> engineVariablesUtils = Mockito.mockStatic(EngineVariablesUtils.class);
             MockedStatic<HttpRequestUtils> httpRequestUtils = Mockito.mockStatic(HttpRequestUtils.class);
        ) {
            engineVariablesUtils.when(() -> EngineVariablesUtils.getTypedVariable(any(HashMap.class), any(String.class), anyBoolean())).thenReturn("VARIABLE");
            engineVariablesUtils.when(() -> EngineVariablesUtils.getTypedVariable(any(HashMap.class), eq(HttpVariablesEnum.HEADERS.getValue()), anyBoolean())).thenReturn(null);
            engineVariablesUtils.when(() -> EngineVariablesUtils.getTypedVariable(any(HashMap.class), eq(HttpVariablesEnum.PATH_PARAMS.getValue()), anyBoolean())).thenReturn(null);
            httpRequestUtils.when(() -> HttpRequestUtils.httpMethodFromValue(any(String.class))).thenReturn(HttpMethod.GET);
            Map<String, Object> variables = mock(HashMap.class);
            HTTPConfiguration httpConfiguration = EngineVariablesToHTTPConfigurationUtils.getHttpConfiguration(variables);
            engineVariablesUtils.verify(() -> EngineVariablesUtils.getTypedVariable(any(HashMap.class), any(String.class), anyBoolean()), times(8));
            httpRequestUtils.verify(() -> HttpRequestUtils.httpMethodFromValue(any(String.class)), times(1));
            assertEquals("VARIABLE", httpConfiguration.getEndpoint());
            assertEquals(HttpMethod.GET, httpConfiguration.getHttpMethod());
            assertEquals("VARIABLE", httpConfiguration.getBody());
            assertEquals(null, httpConfiguration.getHeaders());
            assertEquals(new MultivaluedHashMap(), httpConfiguration.getPathParams());
        }
    }

    @Test
    public void getHttpConfigurationWithPathParamsTest() {
        try (MockedStatic<EngineVariablesUtils> engineVariablesUtils = Mockito.mockStatic(EngineVariablesUtils.class);
             MockedStatic<HttpRequestUtils> httpRequestUtils = Mockito.mockStatic(HttpRequestUtils.class);
        ) {
            //create path params
            MultivaluedHashMap pathParams = new MultivaluedHashMap<>();
            pathParams.put("param1", List.of("value1"));
            engineVariablesUtils.when(() -> EngineVariablesUtils.getTypedVariable(any(HashMap.class), any(String.class), anyBoolean())).thenReturn("VARIABLE");
            engineVariablesUtils.when(() -> EngineVariablesUtils.getTypedVariable(any(HashMap.class), eq(HttpVariablesEnum.HEADERS.getValue()), anyBoolean())).thenReturn(null);
            engineVariablesUtils.when(() -> EngineVariablesUtils.getTypedVariable(any(HashMap.class), eq(HttpVariablesEnum.PATH_PARAMS.getValue()), anyBoolean())).thenReturn(pathParams);
            httpRequestUtils.when(() -> HttpRequestUtils.httpMethodFromValue(any(String.class))).thenReturn(HttpMethod.GET);
            Map<String, Object> variables = mock(HashMap.class);
            HTTPConfiguration httpConfiguration = EngineVariablesToHTTPConfigurationUtils.getHttpConfiguration(variables);
            engineVariablesUtils.verify(() -> EngineVariablesUtils.getTypedVariable(any(HashMap.class), any(String.class), anyBoolean()), times(8));
            httpRequestUtils.verify(() -> HttpRequestUtils.httpMethodFromValue(any(String.class)), times(1));
            assertEquals("VARIABLE", httpConfiguration.getEndpoint());
            assertEquals(HttpMethod.GET, httpConfiguration.getHttpMethod());
            assertEquals("VARIABLE", httpConfiguration.getBody());
            assertEquals(null, httpConfiguration.getHeaders());
            assertEquals(List.of("value1"), httpConfiguration.getPathParams().get("param1"));
        }
    }

}
