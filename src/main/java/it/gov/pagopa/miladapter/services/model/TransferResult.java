package it.gov.pagopa.miladapter.services.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResult {
    private int totalTransfers;
    private int successfulTransfers;
    private int failedTransfers;

    @Builder.Default
    private List<TransferError> errors = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferError {
        private Integer transactionId;
        private Integer transferId;
        private String errorMessage;
        private String errorDetails;
    }

    public boolean hasErrors() {
        return failedTransfers > 0;
    }

    public boolean isFullSuccess() {
        return failedTransfers == 0 && totalTransfers > 0;
    }
}

