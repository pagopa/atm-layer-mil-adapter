package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.HTTPConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EngineVariablesToHTTPConfigurationUtils {

    public static HTTPConfiguration getHttpConfiguration(Map<String, Object> variables) {

        String requestId = UUID.randomUUID().toString();
        String acquirerId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), true);
        String channel = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.CHANNEL.getEngineValue(), true);
        String terminalId = EngineVariablesUtils.getTypedVariable(variables, RequiredProcessVariables.TERMINAL_ID.getEngineValue(), true);

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
                .build();
    }
}
