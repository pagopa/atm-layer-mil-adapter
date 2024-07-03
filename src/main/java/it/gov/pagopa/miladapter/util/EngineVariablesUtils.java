package it.gov.pagopa.miladapter.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

@Slf4j
public class EngineVariablesUtils {

    public static <T> T getTypedVariable(Map<String, Object> map, String variableName, boolean requireNotNull) {
        T value = null;
        if (map.containsKey(variableName)) {
            value = (T) map.get(variableName);
        }
        if (value == null && requireNotNull) {
            log.error("Found null required variable: {}", variableName);
            throw new RuntimeException(String.format("%s variable cannot be null", variableName));
        }
        if (value instanceof String && StringUtils.isBlank((String) value) && requireNotNull) {
            log.error("Found blank required String variable: {}", variableName);
            throw new RuntimeException(String.format("%s String variable cannot be empty", variableName));
        }
        return value;
    }

/*    public static Map<String, Object> getTaskVariablesCaseInsensitive(ExternalTask externalTask) {
        Map<String, Object> variables = externalTask.getAllVariables();
        CaseInsensitiveMap<String, Object> caseInsensitiveMap = new CaseInsensitiveMap<>();
        caseInsensitiveMap.putAll(variables);
        return caseInsensitiveMap;
    }*/
}
