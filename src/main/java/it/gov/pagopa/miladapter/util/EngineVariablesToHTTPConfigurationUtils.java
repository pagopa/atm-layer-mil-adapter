package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.HTTPConfiguration;
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

    public static HTTPConfiguration getHttpConfiguration(Map<String, Object> variables, boolean milFlow) {

        String requestId = UUID.randomUUID().toString();
        String acquirerId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), milFlow);
        String channel = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.CHANNEL.getEngineValue(), milFlow);
        String terminalId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.TERMINAL_ID.getEngineValue(), milFlow);

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
        // Map<String, String> queryParams = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.QUERY_PARAMS.getValue(), false);

        Integer connectionResponseTimeout = getIntegerValue(HttpVariablesEnum.CONNECTION_RESPONSE_TIMEOUT_MILLISECONDS.getValue(), EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.CONNECTION_RESPONSE_TIMEOUT_MILLISECONDS.getValue(), false));
        Integer connectionRequestTimeout = getIntegerValue(HttpVariablesEnum.CONNECTION_REQUEST_TIMEOUT_MILLISECONDS.getValue(), EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.CONNECTION_REQUEST_TIMEOUT_MILLISECONDS.getValue(), false));
        Integer maxRetry = getIntegerValue(HttpVariablesEnum.MAX_RETRY.getValue(), EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.MAX_RETRY.getValue(), false));
        Integer retryIntervalMilliseconds = getIntegerValue(HttpVariablesEnum.RETRY_INTERVAL_MILLISECONDS.getValue(), EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.RETRY_INTERVAL_MILLISECONDS.getValue(), false));

        AuthParameters authParameters = AuthParameters.builder()
                .requestId(requestId)
                .acquirerId(acquirerId)
                .terminalId(terminalId)
                .channel(channel)
                .build();

        return HTTPConfiguration.builder()
                .body(body)
                .endpoint(endpointVariable)
                .httpMethod(httpMethod)
                .pathParams(pathParams)
                .headers(headers)
                .authParameters(authParameters)
                .connectionResponseTimeoutMilliseconds(connectionResponseTimeout)
                .connectionRequestTimeoutMilliseconds(connectionRequestTimeout)
                .maxRetry(maxRetry)
                .retryIntervalMilliseconds(retryIntervalMilliseconds)
                .build();
    }
}
