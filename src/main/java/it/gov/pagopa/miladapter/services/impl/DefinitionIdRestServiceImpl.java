package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.services.DefinitionIdRestService;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DefinitionIdRestServiceImpl extends GenericRestServiceAbstract implements DefinitionIdRestService {




    @Override
    public URI prepareUri(Configuration configuration, String flow) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put(RequiredProcessVariables.ACQUIRER_ID.getModelValue(),
                configuration.getAuthParameters().getAcquirerId());
        pathParams.put(RequiredProcessVariables.BRANCH_ID.getModelValue(),
                configuration.getAuthParameters().getBranchId() != null
                        ? configuration.getAuthParameters().getBranchId()
                        : "_");
        pathParams.put(RequiredProcessVariables.TERMINAL_ID.getModelValue(),
                configuration.getAuthParameters().getTerminalId() != null
                        ? configuration.getAuthParameters().getTerminalId()
                        : configuration.getAuthParameters().getAcquirerId()
                        + configuration.getAuthParameters().getCode());
        pathParams.put(RequiredProcessVariables.FUNCTION_ID.getModelValue(), configuration.getFunction());

        configuration
                .setHttpMethod(HttpMethod.valueOf(restConfigurationProperties.getDefinitionIdProperties().getMethod()));

        return HttpRequestUtils.buildURI(restConfigurationProperties.getModelBasePath(),
                restConfigurationProperties.getDefinitionIdProperties().getUrl(),
                pathParams);
    }

}
