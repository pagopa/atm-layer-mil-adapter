package it.gov.pagopa.miladapter.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbillAbiFederazioneDto {
    private String abi;
    private String pagopaId;
    private String pspFiscalCode;
    private String pspChannel;
    private String password;
    private Boolean pagopaDirect;
}
