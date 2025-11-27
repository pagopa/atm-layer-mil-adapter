package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.client.model.GecGetFeesRequest;
import it.gov.pagopa.miladapter.client.model.GecTransfer;
import it.gov.pagopa.miladapter.client.model.Psp;
import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.properties.GecProperties;
import it.gov.pagopa.miladapter.services.model.GetFeeRequest;
import it.gov.pagopa.miladapter.services.model.GetFeeResponse;
import it.gov.pagopa.miladapter.services.model.Notice;
import it.gov.pagopa.miladapter.services.model.Transfer;
import it.gov.pagopa.miladapter.util.FeeCalculatorErrorCode;
import it.gov.pagopa.miladapter.util.NodeApi;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
@Slf4j
public class FeeCalculatorService {
    private final BasePaymentService basePaymentService;
    private final GecProperties gecProperties;

    public FeeCalculatorService(BasePaymentService basePaymentService, GecProperties gecProperties) {
        this.basePaymentService = basePaymentService;
        this.gecProperties = gecProperties;
    }

    public ResponseEntity<GetFeeResponse> getFee(
            @Valid CommonHeader headers,
            @Valid @NotNull(message = "[" + FeeCalculatorErrorCode.REQUEST_MUST_NOT_BE_EMPTY + "] request must not be empty")
            GetFeeRequest getFeeRequest
    ) {
        log.debug("getFee - Input parameters: {}, body {}", headers, getFeeRequest);

        try {
            PspConfiguration pspConf = this.basePaymentService.retrievePSPConfiguration(headers.getAcquirerId(), NodeApi.FEE).block();
            GecGetFeesRequest gecGetFeeRequest = createGecGetFeeRequest(getFeeRequest, pspConf.getPsp(), headers.getChannel());

            log.debug("Calling GEC service: requestId {}, body {}", headers.getRequestId(), gecGetFeeRequest);

            GetFeeResponse response = this.basePaymentService.getFees(headers.getRequestId(), gecGetFeeRequest).block();

            log.debug("getFee - Response: {}", response);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in getFee", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new Errors(List.of(FeeCalculatorErrorCode.ERROR_RETRIEVING_FEES)).toString()
            );
        }
    }

    /**
     * Create the request to be sent to GEC to retrieve the fees
     *
     * @param getFeeRequest the {@link GetFeeRequest} received by the client
     * @param pspId         the identifier of the PSP
     * @return the {@link GecGetFeesRequest} to be sent to GE
     */
    private GecGetFeesRequest createGecGetFeeRequest(GetFeeRequest getFeeRequest, String pspId, String channel) {

        Notice notice = getFeeRequest.getNotices().get(0); // TODO: change logic when GEC will expose the cart
        List<Psp> idPspList = new ArrayList<>();
        Psp psp = new Psp();
        psp.setIdPsp(pspId);
        idPspList.add(psp);

        List<GecTransfer> transferList = new ArrayList<>();
        for (Transfer transfer : notice.getTransfers()) {
            GecTransfer gecTransfer = new GecTransfer();
            gecTransfer.setCreditorInstitution(transfer.getPaTaxCode());
            if (StringUtils.isNotEmpty(transfer.getCategory())) {
                // the closePayment API of the node does not return a category
                // so the mil-payment-notice return an empty string
                // and will not forward this value to the GEC
                gecTransfer.setTransferCategory(transfer.getCategory());
            }
            transferList.add(gecTransfer);
        }

        GecGetFeesRequest gecGetFeesRequest = new GecGetFeesRequest();
        gecGetFeesRequest.setIdPspList(idPspList);
        gecGetFeesRequest.setPaymentAmount(notice.getAmount());
        gecGetFeesRequest.setPrimaryCreditorInstitution(notice.getPaTaxCode());
        // remapping paymentMethod and touchpoint based on property
        if (getFeeRequest.getPaymentMethod() != null) {
            gecGetFeesRequest.setPaymentMethod(gecProperties.getPaymentmethod().getMap().getOrDefault(getFeeRequest.getPaymentMethod(), "ANY"));
        }
        gecGetFeesRequest.setTouchpoint(gecProperties.getTouchpoint().getMap().getOrDefault(channel, "ANY"));
        gecGetFeesRequest.setTransferList(transferList);

        return gecGetFeesRequest;
    }
}
