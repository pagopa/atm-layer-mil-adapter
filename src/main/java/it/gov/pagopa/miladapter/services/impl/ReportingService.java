package it.gov.pagopa.miladapter.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.miladapter.mapper.PagopaTransactionMapper;
import it.gov.pagopa.miladapter.services.dto.ErrorResponse;
import it.gov.pagopa.miladapter.services.dto.PagopaTransactionsDto;
import it.gov.pagopa.miladapter.services.model.PagoPaTransactionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class ReportingService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PagopaTransactionMapper mapper;

    @Value("${reporting-service.base-url}")
    private String reportingServiceBaseUrl;

    @Value("${reporting-service.pagopa-transactions.path}")
    private String pagopaTransactionsPath;

    @Value("${reporting-service.pagopa-transfer-list.path}")
    private String pagoPaTransferList;

    public ReportingService(RestTemplate restTemplate, ObjectMapper objectMapper, PagopaTransactionMapper mapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.mapper = mapper;
    }

    public ResponseEntity<PagopaTransactionsDto> createTransaction(PagoPaTransactionRequest request) {
        String url = UriComponentsBuilder
                .fromUriString(reportingServiceBaseUrl)
                .path(pagopaTransactionsPath)
                .toUriString();
        PagopaTransactionsDto pagopaTransactionsDtoRequest = mapper.toDto(request);
        try {
            ResponseEntity<PagopaTransactionsDto> response = restTemplate.postForEntity(url, pagopaTransactionsDtoRequest, PagopaTransactionsDto.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response;
            } else {
                log.error("Unexpected response status during transaction creation: {}", response.getStatusCode());
                throw new IllegalStateException("Unexpected response status during transaction creation: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException.BadRequest ex) {
            String body = ex.getResponseBodyAsString();
            try {
                ErrorResponse err = objectMapper.readValue(body, ErrorResponse.class);
                log.error("Error while creating transaction: {}", err.getMessage());
                throw new IllegalArgumentException("Remote service returned 400: " + err.getMessage());
            } catch (JsonProcessingException parseEx) {
                log.error("Error parsing error response body during transaction creation: {}", body, parseEx);
                throw new IllegalArgumentException("Remote service returned 400 and error body could not be parsed: " + body, parseEx);
            }
        }

    }
}
