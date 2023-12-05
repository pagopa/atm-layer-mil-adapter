package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EngineVariablesToHTTPConfigurationUtils {

    public static Integer getIntegerValue(String variableName, String value) {
        int integerValue;
        if (value == null) {
            return null;
        }
        if (StringUtils.isBlank(value)) {
            throw new RuntimeException(String.format("%s cannot be empty", variableName));
        }
        try {
            integerValue = Integer.parseInt(value);
        } catch (Exception e) {
            throw new RuntimeException(String.format("%s must be an integer", variableName));
        }
        return integerValue;
    }

    public static Integer parseInteger(Long value) {
        if (value != null) {
            return value.intValue();
        }
        return null;
    }

    public static Configuration getHttpConfigurationExternalCall(Map<String, Object> variables, boolean milFlow) {

        String requestId = UUID.randomUUID().toString();
        String acquirerId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), milFlow);
        String channel = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.CHANNEL.getEngineValue(), milFlow);
        String terminalId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.TERMINAL_ID.getEngineValue(), milFlow);
        Long delayMilliseconds = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.DELAY_MILLISECONDS.getValue(), false);


        String endpointVariable = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.URL.getValue(), true);
        String httpMethodVariable = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.METHOD.getValue(), true);
        HttpMethod httpMethod = HttpRequestUtils.httpMethodFromValue(httpMethodVariable);
        String body = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.BODY.getValue(), false);
        Map<String, String> headersMap = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.HEADERS.getValue(), false);
        HttpHeaders headers = HttpRequestUtils.createHttpHeaders(headersMap);
        headers.add(RequiredProcessVariables.REQUEST_ID.getMilValue(), UUID.randomUUID().toString());
        Map<String, String> pathParams = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.PATH_PARAMS.getValue(), false);
        if (CollectionUtils.isEmpty(pathParams)) {
            pathParams = new HashMap<>();
        }
        HttpRequestUtils.checkNotNullPathParams(pathParams);

        Long connectionResponseTimeout = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.CONNECTION_RESPONSE_TIMEOUT_MILLISECONDS.getValue(), false);
        Long connectionRequestTimeout = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.CONNECTION_REQUEST_TIMEOUT_MILLISECONDS.getValue(), false);
        Long maxRetry = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.MAX_RETRY.getValue(), false);
        Long retryIntervalMilliseconds = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.RETRY_INTERVAL_MILLISECONDS.getValue(), false);

        AuthParameters authParameters = AuthParameters.builder().requestId(requestId).acquirerId(acquirerId).terminalId(terminalId).channel(channel).build();

        return Configuration.builder()
                .body(body)
                .endpoint(endpointVariable)
                .httpMethod(httpMethod)
                .pathParams(pathParams)
                .headers(headers)
                .authParameters(authParameters)
                .connectionResponseTimeoutMilliseconds(parseInteger(connectionResponseTimeout))
                .connectionRequestTimeoutMilliseconds(parseInteger(connectionRequestTimeout))
                .maxRetry(parseInteger(maxRetry))
                .retryIntervalMilliseconds(parseInteger(retryIntervalMilliseconds))
                .delayMilliseconds(parseInteger(delayMilliseconds))
                .build();
    }

    public static Configuration getHttpConfigurationInternalCall(Map<String, Object> variables, boolean milFlow) {

        String acquirerId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), milFlow);
        String terminalId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.TERMINAL_ID.getEngineValue(), milFlow);
        String branchId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.BRANCH_ID.getEngineValue(), false);
        String code = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.CODE.getEngineValue(), false);

        String functionId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.FUNCTION_ID.getEngineValue(), false);
        Long delayMilliseconds = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.DELAY_MILLISECONDS.getValue(), false);

        Long connectionResponseTimeout = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.CONNECTION_RESPONSE_TIMEOUT_MILLISECONDS.getValue(), false);
        Long connectionRequestTimeout = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.CONNECTION_REQUEST_TIMEOUT_MILLISECONDS.getValue(), false);
        Long maxRetry = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.MAX_RETRY.getValue(), false);
        Long retryIntervalMilliseconds = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.RETRY_INTERVAL_MILLISECONDS.getValue(), false);

        AuthParameters authParameters = AuthParameters.builder().acquirerId(acquirerId).terminalId(terminalId).branchId(branchId).code(code).build();

        return Configuration.builder()
                .authParameters(authParameters)
                .connectionResponseTimeoutMilliseconds(parseInteger(connectionResponseTimeout))
                .connectionRequestTimeoutMilliseconds(parseInteger(connectionRequestTimeout))
                .maxRetry(parseInteger(maxRetry))
                .retryIntervalMilliseconds(parseInteger(retryIntervalMilliseconds))
                .function(functionId)
                .delayMilliseconds(parseInteger(delayMilliseconds))
                .build();
    }
}
