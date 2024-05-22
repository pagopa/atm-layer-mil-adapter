package it.gov.pagopa.miladapter.util;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EngineVariablesUtilsTest {
    Map<String, Object> map;

    @BeforeEach
    public void init() {
        map = getMapForTest();
    }

    @Test
    void getTypedVariableTestOk() {
        String value = EngineVariablesUtils.getTypedVariable(map, "key", true);
        assertEquals("value", value);
    }

    @Test
    void getTypedVariableTestRequiredNull() {
        Exception exception = assertThrows(Exception.class, () -> EngineVariablesUtils.getTypedVariable(map, "inexistentValue", true));
        assertEquals("inexistentValue variable cannot be null", exception.getMessage());
    }

    @Test
    void getTypedVariableTestNotRequiredNull() {
        String value = EngineVariablesUtils.getTypedVariable(map, "inexistentValue", false);
        assertEquals(null, value);
    }

    @Test
    void getTypedVariableTestRequiredBlankString() {
        Exception exception = assertThrows(RuntimeException.class, () -> EngineVariablesUtils.getTypedVariable(map, "blankSpaceKey", true));
        assertEquals("blankSpaceKey String variable cannot be empty", exception.getMessage());
    }

    @Test
    void getTypedVariableTestRequiredEmptyString() {
        Exception exception = assertThrows(RuntimeException.class, () -> EngineVariablesUtils.getTypedVariable(map, "emptyKey", true));
        assertEquals("emptyKey String variable cannot be empty", exception.getMessage());
    }

    @Test
    void getTypedVariableTestNotRequiredBlankString() {
        String value = EngineVariablesUtils.getTypedVariable(map, "blankSpaceKey", false);
        assertEquals(" ", value);
    }

    @Test
    void getTypedVaribaleTestNotRequiredEmptyString() {
        String value = EngineVariablesUtils.getTypedVariable(map, "emptyKey", false);
        assertEquals("", value);
    }

    @Test
    void getTaskVariablesCaseInsensitiveTest() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getAllVariables()).thenReturn(map);
        Map<String, Object> result = EngineVariablesUtils.getTaskVariablesCaseInsensitive(externalTask);
        assertEquals("VALUE", result.get("key"));
        assertEquals("VALUE", result.get("KEY"));
        assertEquals(3, result.size());
    }

    @Test
    void defaultConstructorTest() {
        Object engineVariablesUtils = new EngineVariablesUtils();
        assertTrue(engineVariablesUtils instanceof EngineVariablesUtils);
    }

    public Map<String, Object> getMapForTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("blankSpaceKey", " ");
        map.put("emptyKey", "");
        map.put("KEY", "VALUE");
        return map;
    }
}
