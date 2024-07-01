package it.gov.pagopa.miladapter.services.impl;

import camundajar.impl.com.google.gson.JsonObject;
import io.opentelemetry.api.trace.SpanBuilder;
import it.gov.pagopa.miladapter.dto.CamundaWaitMessage;
import it.gov.pagopa.miladapter.dto.ProcessVariable;
import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.CallbackCamundaService;
import it.gov.pagopa.miladapter.services.ExternalCallService;
import it.gov.pagopa.miladapter.services.MILRestExternalService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.variable.ClientValues;
import org.camunda.bpm.client.variable.value.JsonValue;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

import static org.camunda.spin.Spin.JSON;

@Slf4j
@Service
public class ExternalCallServiceImpl extends GenericRestExternalServiceAbstract implements ExternalCallService {


    @Override
    public URI prepareUri(Configuration configuration) {
        return HttpRequestUtils.buildURI(restConfigurationProperties.getMilBasePath(), configuration.getEndpoint(), configuration.getPathParams());
    }

    @Override
    public RestTemplate getRestTemplate(Configuration configuration) {
        return super.getRestTemplate(configuration);
    }

    @Override
    public SpanBuilder spanBuilder(Configuration configuration) {
        return super.spanBuilder(configuration);
    }

    @Autowired
    RestConfigurationProperties restConfigurationProperties;

    @Autowired
    RestTemplateGenerator restTemplateGenerator;

    @Autowired
    CallbackCamundaService callbackCamundaService;

    @Async
    public void executeCallAndCallBack (Map<String, Object> body) {
        Configuration configuration = EngineVariablesToHTTPConfigurationUtils.getHttpConfigurationExternalCallNew(body);
        VariableMap response = callExternalService(configuration);
        callBackEngine(body, response);
    }

    private void callBackEngine(Map<String, Object> body, VariableMap response) {
        try {
            CamundaWaitMessage camundaWaitMessage = createCallbackPayload(body, response);
            callbackCamundaService.callAdapter(camundaWaitMessage);
        } catch (Exception e) {
            log.error("Error while invoking callback", e);
        }
    }

    private  VariableMap callExternalService(Configuration configuration) {
        HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
        ResponseEntity<String> response;
        URI url = this.prepareUri(configuration);
        HttpEntity<String> entity = HttpRequestUtils.buildHttpEntity(configuration.getBody(), configuration.getHeaders());
        response = this.getRestTemplate(configuration).exchange(url, configuration.getHttpMethod(), entity, String.class);
        VariableMap output = Variables.createVariables();
        if (response.getBody() == null) {
            response = new ResponseEntity<>(new JsonObject().toString(), response.getStatusCode());
        }
        JsonValue jsonValue;
        if (StringUtils.isNotBlank(response.getBody())
                && response.getStatusCode() != null
                && response.getStatusCode().is2xxSuccessful()) {
            jsonValue = ClientValues.jsonValue(response.getBody());
        } else {
            jsonValue = ClientValues.jsonValue("{}");
        }
        output.putValue(HttpVariablesEnum.RESPONSE.getValue(), jsonValue);
        output.putValue(HttpVariablesEnum.STATUS_CODE.getValue(), response.getStatusCode().value());
        SpinJsonNode headersJsonNode = JSON(response.getHeaders());
        output.putValue(HttpVariablesEnum.RESPONSE_HEADERS.getValue(), headersJsonNode.toString());
        return output;
    }

    private CamundaWaitMessage createCallbackPayload(Map<String, Object> body, VariableMap response) {
        CamundaWaitMessage camundaWaitMessage = new CamundaWaitMessage();
        camundaWaitMessage.setMessageName((String) body.get("responseEventMessage"));
        camundaWaitMessage.setProcessInstanceId((String) body.get("processInstanceId"));

        String responseBody = response.getValueTyped("response").getValue().toString();
        String statusCode = response.getValueTyped("statusCode").getValue().toString();

        camundaWaitMessage.setProcessVariables(Map.of(
                "statusCode", new ProcessVariable(statusCode, "String"),
                "response", new ProcessVariable(responseBody, "String")
        ));
        return camundaWaitMessage;
    }

    @Override
    public VariableMap executeRestCall(Configuration configuration) {
        return super.executeRestCall(configuration);
    }

    @Override
    public void injectAuthToken(Configuration configuration) {

    }






}
