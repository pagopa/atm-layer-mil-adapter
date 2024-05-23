package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.services.IDPayRestService;
import it.gov.pagopa.miladapter.util.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@Slf4j
public class IDPayRestServiceImpl extends GenericRestServiceAbstract implements IDPayRestService {

    @Override
    public void injectAuthToken(Configuration configuration) {
        tokenService.injectAuthToken(configuration.getHeaders(), configuration.getAuthParameters());
    }

    @Override
    public URI prepareUri(Configuration configuration) {
        return HttpRequestUtils.buildURI(restConfigurationProperties.getIdPayBasePath(), configuration.getEndpoint(), configuration.getPathParams());
    }

}
