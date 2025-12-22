package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.mapper.PagopaTransactionMapper;
import it.gov.pagopa.miladapter.mapper.PagopaTransferMapper;
import it.gov.pagopa.miladapter.services.dto.ErrorResponse;
import it.gov.pagopa.miladapter.services.dto.PagopaTransactionsDto;
import it.gov.pagopa.miladapter.services.dto.PagopaTransferListDto;
import it.gov.pagopa.miladapter.services.exception.ReportingServiceException;
import it.gov.pagopa.miladapter.services.model.PagoPaTransactionRequest;
import it.gov.pagopa.miladapter.services.model.PagoPaTransferListRequest;
import it.gov.pagopa.miladapter.services.model.TransferResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@Slf4j
public class ReportingService {
  private final RestTemplate restTemplate;
    private final PagopaTransactionMapper pagopaTransactionMapper;
  private final PagopaTransferMapper pagopaTransferMapper;

  @Value("${reporting-service.base-url}")
  private String reportingServiceBaseUrl;

  @Value("${reporting-service.pagopa-transactions.path}")
  private String pagopaTransactionsPath;

  @Value("${reporting-service.pagopa-transfer-list.path}")
  private String pagoPaTransferList;

  @Value("${reporting-service.fail-on-partial-error:true}")
  private boolean failOnPartialError;

  public ReportingService(
      RestTemplate restTemplate,
      PagopaTransactionMapper pagopaTransactionMapper,
      PagopaTransferMapper pagopaTransferMapper) {
    this.restTemplate = restTemplate;
      this.pagopaTransactionMapper = pagopaTransactionMapper;
    this.pagopaTransferMapper = pagopaTransferMapper;
  }

  public ResponseEntity<PagopaTransactionsDto> createTransaction(PagoPaTransactionRequest request) {
    PagopaTransactionsDto dtoRequest = pagopaTransactionMapper.toDto(request);
    String url = buildUrl(pagopaTransactionsPath);

    log.debug("Creating transaction with id [{}] at URL: {}", dtoRequest.getTransactionId(), url);

    return postEntity(
        url, dtoRequest, PagopaTransactionsDto.class, "transaction", dtoRequest.getTransactionId());
  }

  public ResponseEntity<String> createTransferList(PagoPaTransferListRequest request) {
    List<PagopaTransferListDto> transferList = pagopaTransferMapper.toDtoList(request);
    String url = buildUrl(pagoPaTransferList);

    log.debug("Creating {} transfers at URL: {}", transferList.size(), url);

    TransferResult result = processTransferList(url, transferList);

    if (result.hasErrors()) {
      log.warn(
          "Transfer list processing completed with errors. Success: {}, Failed: {}",
          result.getSuccessfulTransfers(),
          result.getFailedTransfers());

      if (failOnPartialError) {
        throw new ReportingServiceException(
            String.format(
                "Failed to create %d out of %d transfers",
                result.getFailedTransfers(), result.getTotalTransfers()),
            HttpStatus.PARTIAL_CONTENT);
      }
    }

    log.debug(
        "Transfer list created successfully. Total: {}, Success: {}, Failed: {}",
        result.getTotalTransfers(),
        result.getSuccessfulTransfers(),
        result.getFailedTransfers());

    return ResponseEntity.ok(
        String.format(
            "Transfer list processing completed. Success: %d, Failed: %d",
            result.getSuccessfulTransfers(), result.getFailedTransfers()));
  }

  private TransferResult processTransferList(String url, List<PagopaTransferListDto> transferList) {
    TransferResult.TransferResultBuilder resultBuilder =
        TransferResult.builder()
            .totalTransfers(transferList.size())
            .successfulTransfers(0)
            .failedTransfers(0);

    for (PagopaTransferListDto transfer : transferList) {
      try {
        postEntity(
            url,
            transfer,
            PagopaTransferListDto.class,
            "transfer",
            transfer.getTransactionId() + "/" + transfer.getTransferId());
        resultBuilder.successfulTransfers(resultBuilder.build().getSuccessfulTransfers() + 1);
      } catch (ReportingServiceException ex) {
        log.error(
            "Failed to create transfer with transactionId [{}] and transferId [{}]: {}",
            transfer.getTransactionId(),
            transfer.getTransferId(),
            ex.getMessage());

        TransferResult currentResult = resultBuilder.build();
        resultBuilder.failedTransfers(currentResult.getFailedTransfers() + 1);

        TransferResult.TransferError error =
            TransferResult.TransferError.builder()
                .transactionId(transfer.getTransactionId())
                .transferId(transfer.getTransferId())
                .errorMessage(ex.getMessage())
                .errorDetails(ex.getResponseBody())
                .build();

        currentResult.getErrors().add(error);

        if (failOnPartialError) {
          throw ex;
        }
      }
    }

    return resultBuilder.build();
  }

  private <T> ResponseEntity<T> postEntity(
      String url, Object request, Class<T> responseType, String entityType, Object entityId) {
    try {
      ResponseEntity<T> response = restTemplate.postForEntity(url, request, responseType);

      if (response.getStatusCode().is2xxSuccessful()) {
        log.debug("Successfully created {} with id [{}]", entityType, entityId);
        return response;
      } else {
        log.error(
            "Unexpected response status during {} creation with id [{}]: {}",
            entityType,
            entityId,
            response.getStatusCode());
        throw new ReportingServiceException(
            String.format(
                "Unexpected response status during %s creation: %s",
                entityType, response.getStatusCode()),
            response.getStatusCode());
      }
    } catch (HttpClientErrorException.BadRequest ex) {
      handleBadRequestError(ex, entityType, entityId);
      throw new IllegalStateException("Should not reach here"); // This line will never be reached
    }
  }

  private void handleBadRequestError(
      HttpClientErrorException.BadRequest ex, String entityType, Object entityId) {
    String message = ex.getResponseBodyAsString();

      ErrorResponse errorResponse = new ErrorResponse(message);
      String errorMessage = errorResponse.getMessage();

      log.error("Error while creating {} with id [{}]: {}", entityType, entityId, errorMessage);

      throw new ReportingServiceException(
          String.format(
              "Remote service returned 400 during %s creation: %s", entityType, errorMessage),
          HttpStatus.BAD_REQUEST,
          message);
  }

  private String buildUrl(String path) {
    return UriComponentsBuilder.fromUriString(reportingServiceBaseUrl).path(path).toUriString();
  }
}
