package it.gov.pagopa.miladapter.services.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagopaTransferListDto {

    @NotNull
    private Long transactionId;

    @NotNull
    private Integer transferId;

    @NotNull
    private BigDecimal transferAmount;

    @Size(max = 35)
    private String transferCro;

    @Size(max = 35)
    private String flowId;

    @NotNull
    private Boolean pagopaReported = false;

    private LocalDate transferExecutionDt;

    @NotNull
    @Size(max = 11)
    private String paFiscalCode;

    @Size(max = 140)
    private String paName;

    @NotNull
    @Size(max = 34)
    private String paIban;

    @NotNull
    @Size(max = 140)
    private String rmtInfo;
}

