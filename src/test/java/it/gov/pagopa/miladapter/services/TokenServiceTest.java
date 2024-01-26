package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.KeyToken;
import it.gov.pagopa.miladapter.model.Token;
import it.gov.pagopa.miladapter.properties.AuthProperties;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TokenServiceTest {
    @InjectMocks
    private TokenService tokenService;
    @Mock
    private RestConfigurationProperties restConfigurationProperties;
    @Mock
    CacheService cacheService;
    @Mock
    RestTemplate restTemplate;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInjectAuthTokenPresentInCache() {
        HttpHeaders restHeaders = new HttpHeaders();
        AuthParameters authParameters = getAuthParameters();
        AuthProperties authProperties = getAuthProperties();
        Token validToken = getToken();
        when(restConfigurationProperties.getAuth()).thenReturn(authProperties);
        ResponseEntity<Token> mockResponseEntity = new ResponseEntity<>(validToken, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Token.class)))
                .thenReturn(mockResponseEntity);
        tokenService.injectAuthToken(restHeaders, authParameters);
        assertTrue(restHeaders.containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testInjectAuthTokenNotPresentInCacheOk() {
        HttpHeaders restHeaders = new HttpHeaders();
        AuthParameters authParameters = getAuthParameters();
        AuthProperties authProperties = getAuthProperties();
        Token expectedToken = getToken();
        when(restConfigurationProperties.getAuth()).thenReturn(authProperties);
        when(restConfigurationProperties.getMilAuthenticatorBasePath()).thenReturn("test");
        when(cacheService.getToken(any(KeyToken.class))).thenReturn(Optional.empty());
        ResponseEntity<Token> mockResponseEntity = new ResponseEntity<>(expectedToken, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Token.class)))
                .thenReturn(mockResponseEntity);
        tokenService.injectAuthToken(restHeaders, authParameters);
        assertTrue(restHeaders.containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testInjectAuthTokenNotPresentInCacheKo() {
        HttpHeaders restHeaders = new HttpHeaders();
        AuthParameters authParameters = getAuthParameters();
        AuthProperties authProperties = getAuthProperties();
        Token expectedToken = getToken();
        when(restConfigurationProperties.getAuth()).thenReturn(authProperties);
        when(restConfigurationProperties.getMilAuthenticatorBasePath()).thenReturn("test");
        when(cacheService.getToken(any(KeyToken.class))).thenReturn(Optional.empty());
        ResponseEntity<Token> mockResponseEntity = new ResponseEntity<>(expectedToken, HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Token.class)))
                .thenReturn(mockResponseEntity);
        assertThrows(RuntimeException.class, () -> {
            tokenService.injectAuthToken(restHeaders, authParameters);
        });
    }

    @Test
    void testGenerateToken() {
        AuthParameters authParameters = getAuthParameters();
        AuthProperties authProperties = getAuthProperties();
        KeyToken keyToken = getKeyToken();
        when(restConfigurationProperties.getMilAuthenticatorBasePath()).thenReturn("milAuthenticatorBasePath");
        when(restConfigurationProperties.getAuth()).thenReturn(authProperties);
        ResponseEntity<Token> expectedResponse = new ResponseEntity<>(getToken(), new HttpHeaders(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Token.class))).thenReturn(expectedResponse);
        Token result = tokenService.generateToken(authParameters, keyToken);
        assertEquals(expectedResponse.getBody(), result);
    }

    private static AuthParameters getAuthParameters() {
        AuthParameters authParameters = new AuthParameters();
        authParameters.setRequestId("6762543c-2660-4622-b4d4-8b2bc596df29");
        authParameters.setTerminalId("64874412");
        authParameters.setAcquirerId("06789");
        authParameters.setChannel("ATM");
        authParameters.setTransactionId("123");
        return authParameters;
    }

    private static AuthProperties getAuthProperties() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setClientSecret("bea0fc26-fe22-4b26-8230-ef7d4461acf9");
        authProperties.setMilAuthenticatorPath("/MAP");
        authProperties.setClientId("83c0b10f-b398-4cc8-b356-a3e0f0291679");
        authProperties.setGrantType("client_credentials");
        return authProperties;
    }

    private static Token getToken() {
        Token token = new Token();
        token.setAccess_token("valid_token_value");
        token.setToken_type("Bearer");
        return token;
    }

    private static KeyToken getKeyToken() {
        KeyToken keyToken = new KeyToken();
        keyToken.setTerminalId("terminalId");
        keyToken.setAcquirerId("acquirerId");
        keyToken.setChannel("channel");
        keyToken.setTransactionId("transactionId");
        return keyToken;
    }
}
