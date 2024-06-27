package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class EngineVariablesToHTTPConfigurationUtils {
    private static String idPayKey;

    @Value("${id-pay.api-key}")
    public static void setIdPayKey(String idPayKey) {
        EngineVariablesToHTTPConfigurationUtils.idPayKey = idPayKey;
    }

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

    public static Integer parseInteger(Number input) {
        if (input == null) {
            return null;
        }
        if (input instanceof Long) {
            return Math.toIntExact((Long) input);
        } else if (input instanceof Integer) {
            return (Integer) input;
        } else {
            throw new IllegalArgumentException("Cannot cast delayMilliseconds value, input class: " + input.getClass().getName());
        }
    }

    public static Configuration getHttpConfigurationExternalCall(Map<String, Object> variables, boolean milFlow, boolean idPayFlow) {
        String requestId = UUID.randomUUID().toString();
        String acquirerId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), milFlow);
        String channel = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.CHANNEL.getEngineValue(), milFlow);
        String terminalId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.TERMINAL_ID.getEngineValue(), milFlow);
        String transactionId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), milFlow);
        Number delayMilliseconds = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.DELAY_MILLISECONDS.getValue(), false);
        String endpointVariable = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.URL.getValue(), true);
        String httpMethodVariable = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.METHOD.getValue(), true);
        String accessToken = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.ACCESS_TOKEN.getValue(), true);
        HttpMethod httpMethod = HttpRequestUtils.httpMethodFromValue(httpMethodVariable);
        String body = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.BODY.getValue(), false);
        Map<String, String> headersMap = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.HEADERS.getValue(), false);
        HttpHeaders headers = HttpRequestUtils.createHttpHeaders(headersMap);
        headers.add(RequiredProcessVariables.REQUEST_ID.getAuthenticatorValue(), UUID.randomUUID().toString());
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer".concat(" ").concat(accessToken));
        if (idPayFlow) {
            headers.add(RequiredProcessVariables.IDPAY_KEY.getEngineValue(), idPayKey);
        }
        Map<String, String> pathParams = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.PATH_PARAMS.getValue(), false);
        if (CollectionUtils.isEmpty(pathParams)) {
            pathParams = new HashMap<>();
        }
        HttpRequestUtils.checkNotNullPathParams(pathParams);
        Number connectionResponseTimeout = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.CONNECTION_RESPONSE_TIMEOUT_MILLISECONDS.getValue(), false);
        Number connectionRequestTimeout = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.CONNECTION_REQUEST_TIMEOUT_MILLISECONDS.getValue(), false);
        Number maxRetry = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.MAX_RETRY.getValue(), false);
        Number retryIntervalMilliseconds = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.RETRY_INTERVAL_MILLISECONDS.getValue(), false);
        String parentSpanContextString = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.ACTIVITY_PARENT_SPAN.getEngineValue(), false);
        AuthParameters authParameters = AuthParameters.builder().requestId(requestId).acquirerId(acquirerId).terminalId(terminalId).channel(channel).transactionId(transactionId).build();
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
                .parentSpanContextString(parentSpanContextString)
                .build();
    }

    public static Configuration getHttpConfigurationInternalCall(Map<String, Object> variables, boolean milFlow) {
        String acquirerId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), milFlow);
        String terminalId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.TERMINAL_ID.getEngineValue(), milFlow);
        String branchId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.BRANCH_ID.getEngineValue(), false);
        String code = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.CODE.getEngineValue(), false);
        String functionId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.FUNCTION_ID.getEngineValue(), false);
        Number delayMilliseconds = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.DELAY_MILLISECONDS.getValue(), false);
        Number connectionResponseTimeout = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.CONNECTION_RESPONSE_TIMEOUT_MILLISECONDS.getValue(), false);
        Number connectionRequestTimeout = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.CONNECTION_REQUEST_TIMEOUT_MILLISECONDS.getValue(), false);
        Number maxRetry = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.MAX_RETRY.getValue(), false);
        Number retryIntervalMilliseconds = EngineVariablesUtils.getTypedVariable(variables, HttpVariablesEnum.RETRY_INTERVAL_MILLISECONDS.getValue(), false);
        String parentSpanContextString = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.ACTIVITY_PARENT_SPAN.getEngineValue(), false);
        AuthParameters authParameters = AuthParameters.builder().acquirerId(acquirerId).terminalId(terminalId).branchId(branchId).code(code).build();
        return Configuration.builder()
                .authParameters(authParameters)
                .connectionResponseTimeoutMilliseconds(parseInteger(connectionResponseTimeout))
                .connectionRequestTimeoutMilliseconds(parseInteger(connectionRequestTimeout))
                .maxRetry(parseInteger(maxRetry))
                .retryIntervalMilliseconds(parseInteger(retryIntervalMilliseconds))
                .function(functionId)
                .delayMilliseconds(parseInteger(delayMilliseconds))
                .parentSpanContextString(parentSpanContextString)
                .build();
    }
}
