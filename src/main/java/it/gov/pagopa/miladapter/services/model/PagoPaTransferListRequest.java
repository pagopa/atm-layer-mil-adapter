package it.gov.pagopa.miladapter.services.model;

import java.math.BigInteger;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoPaTransferListRequest {
    private BigInteger transactionId;
    List<Transfer> transfers;
    private Boolean pagopaReported = false;
}
