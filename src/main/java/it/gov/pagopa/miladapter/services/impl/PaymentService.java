package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.services.model.*;
import it.gov.pagopa.miladapter.util.ErrorCode;
import it.gov.pagopa.miladapter.util.PaymentNoticeConstants;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.StPaymentTokens;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
@Slf4j
public class PaymentService {
    private final BasePaymentService basePaymentService;

    public PaymentService(BasePaymentService basePaymentService) {
        this.basePaymentService = basePaymentService;
    }

    /**
     * Calls the node to pass the outcome of the e-money transaction
     * The HTTP response contains a Location header with the URL to invoke to retrieve the final status of the closing operation.
     *
     * @param headers the object containing all the common headers used by the mil services
     * @param transactionId the transaction ID of the e-money transaction
     * @param closePaymentRequest a {@link ClosePaymentRequest} instance containing the outcome of the e-payment transaction
     * @return a {@link ClosePaymentResponse} instance containing the remapped outcome from the node
     */
    public ResponseEntity<ClosePaymentResponse> sendPaymentOutcome(
            @Valid CommonHeader headers,
            @Pattern(regexp = PaymentNoticeConstants.TRANSACTION_ID_REGEX,
                    message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP + "] transactionId must match \"{regexp}\"")
            String transactionId,
            @Valid
            @NotNull(message = "[" + ErrorCode.CLOSE_REQUEST_MUST_NOT_BE_EMPTY + "] request must not be empty")
            ClosePaymentRequest closePaymentRequest) {

        log.debug("closePayment with SendPaymentOutcome - Input parameters: {}, transactionId : {}, {}",
                headers, transactionId, closePaymentRequest);

        PspConfiguration pspConf = this.basePaymentService.retrievePSPConfiguration(headers.getAcquirerId());
        return this.callNodeSendPaymentOutcome(pspConf, closePaymentRequest);
    }

    /**
     * Branch of the sendPaymentOutcome.
     */
    private ResponseEntity<ClosePaymentResponse> callNodeSendPaymentOutcome(PspConfiguration pspConf, ClosePaymentRequest closePaymentRequest) {
        SendPaymentOutcomeV2Request req = new SendPaymentOutcomeV2Request();
        req.setIdPSP(pspConf.getPsp());
        req.setIdBrokerPSP(pspConf.getBroker());
        req.setIdChannel(pspConf.getChannel());
        req.setPassword(pspConf.getPassword());

        StPaymentTokens stPaymentTokens = new StPaymentTokens();
        stPaymentTokens.getPaymentToken().addAll(closePaymentRequest.getPaymentTokens());
        req.setPaymentTokens(stPaymentTokens);
        req.setOutcome(closePaymentRequest.getOutcome().equals(PaymentTransactionOutcome.CLOSE.name()) ? StOutcome.OK : StOutcome.KO);

        try {
            SendPaymentOutcomeV2Response outcomeResponse = this.basePaymentService.sendPaymentOutcomeV2(req);
            if (outcomeResponse == null) {
                log.error("[{}] Null response from the node sendPaymentOutcomeV2 service", ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)).toString());
            }
            ClosePaymentResponse response = this.buildResponse(outcomeResponse);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            log.error("[{}] Error calling the node sendPaymentOutcomeV2 service", ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)).toString());
        }
    }

    private ClosePaymentResponse buildResponse(SendPaymentOutcomeV2Response outcomeResponse) {
        if (Outcome.OK.name().equals(outcomeResponse.getOutcome().value())) {
            ClosePaymentResponse closePaymentResponse = new ClosePaymentResponse();
            closePaymentResponse.setOutcome(outcomeResponse.getOutcome().value());
            return closePaymentResponse;
        } else {
            return this.buildResponseKo(outcomeResponse);
        }
    }

    private ClosePaymentResponse buildResponseKo(SendPaymentOutcomeV2Response outcomeResponse) {
        ClosePaymentResponse closePaymentResponse = new ClosePaymentResponse();
        closePaymentResponse.setOutcome(
                this.basePaymentService.remapNodeFaultToOutcome(
                        outcomeResponse.getFault().getFaultCode(),
                        outcomeResponse.getFault().getOriginalFaultCode()
                ));
        closePaymentResponse.setFault(this.basePaymentService.setFaultDetails(outcomeResponse.getFault()));
        log.error("Node sendPaymentOutcomeV2 responded with fault [{}] and fault code [{}]",
                outcomeResponse.getFault().getFaultString(), outcomeResponse.getFault().getFaultCode());
        return closePaymentResponse;
    }
}
