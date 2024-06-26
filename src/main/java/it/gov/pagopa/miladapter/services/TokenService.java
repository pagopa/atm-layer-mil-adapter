package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.KeyToken;
import it.gov.pagopa.miladapter.model.Token;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;

@Service
@Slf4j
public class TokenService {

    @Autowired
    private RestConfigurationProperties restConfigurationProperties;
    @Autowired
    CacheService cacheService;
    @Autowired
    RestTemplate restTemplate;



    public void injectAuthToken(HttpHeaders restHeaders, AuthParameters authParameters) {
        String accessToken = this.getTokenMilAuth(authParameters);
        log.info("MIL Authentication Completed. JWT Token acquired");
        restHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer".concat(" ").concat(accessToken));
    }


    private String getTokenMilAuth(AuthParameters authParameters) {
        KeyToken keyToken = new KeyToken();
        keyToken.setChannel(authParameters.getChannel());
        keyToken.setAcquirerId(authParameters.getAcquirerId());
        keyToken.setTerminalId(authParameters.getTerminalId());
        keyToken.setTransactionId(authParameters.getTransactionId());
        HttpHeaders headers = prepareAuthHeaders(authParameters);
        HttpEntity<?> request = new HttpEntity<>(headers);
        String externalApiUrl = restConfigurationProperties.getMilAuthenticatorBasePath() + restConfigurationProperties.getAuth().getMilAuthenticatorPath();
        ResponseEntity<Token> response = restTemplate.exchange(externalApiUrl, HttpMethod.GET, request, Token.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("There was an error during the API call" + response.getStatusCode());
        }
        Token token = response.getBody();
        return token.getAccess_token();
    }

    public Token generateToken(AuthParameters authParameters, KeyToken keyToken) {
        log.info("MIL Authentication flow started");
        HttpHeaders headers = prepareAuthHeaders(authParameters);

        HttpEntity<MultiValueMap<String, String>> request = prepareAuthBody(headers);
        String externalApiUrl = restConfigurationProperties.getMilAuthenticatorBasePath() + restConfigurationProperties.getAuth().getMilAuthenticatorPath();
        ResponseEntity<Token> response = restTemplate.exchange(externalApiUrl, HttpMethod.POST, request, Token.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("There was an error during the API call" + response.getStatusCode());
        }

        Token token = response.getBody();

        if (token != null) {
            token.setTime(Calendar.getInstance().getTimeInMillis());
            token.setExpires_in(cacheService.calculateTokenDurationInSeconds(token.getExpires_in()));
            cacheService.insertToken(keyToken, token);
        }

        return token;
    }


    private HttpEntity<MultiValueMap<String, String>> prepareAuthBody(HttpHeaders headers) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(RequiredProcessVariables.CLIENT_ID.getAuthenticatorValue(), restConfigurationProperties.getAuth().getClientId());
        body.add(RequiredProcessVariables.CLIENT_SECRET.getAuthenticatorValue(), restConfigurationProperties.getAuth().getClientSecret());
        body.add(RequiredProcessVariables.GRANT_TYPE.getAuthenticatorValue(), restConfigurationProperties.getAuth().getGrantType());

        return new HttpEntity<>(body, headers);
    }

    private static HttpHeaders prepareAuthHeaders(AuthParameters authParameters) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        headers.add(RequiredProcessVariables.REQUEST_ID.getAuthenticatorValue(), authParameters.getRequestId());
        headers.add(RequiredProcessVariables.ACQUIRER_ID.getAuthenticatorValue(), authParameters.getAcquirerId());
        headers.add(RequiredProcessVariables.CHANNEL.getAuthenticatorValue(), authParameters.getChannel());
        headers.add(RequiredProcessVariables.TERMINAL_ID.getAuthenticatorValue(), authParameters.getTerminalId());
        headers.add(RequiredProcessVariables.TRANSACTION_ID.getAuthenticatorValue(), authParameters.getTransactionId());
        return headers;
    }
}
