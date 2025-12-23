package it.gov.pagopa.miladapter.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Class representation of the QR-Code encoding the data of a payment notice
 * @see <a href="https://docs.pagopa.it/avviso-pagamento/allegato-2/specifiche-tecniche/dati-per-il-pagamento/codice-qr">QR Code specification</a>
 */
@Setter
@Getter
public class QrCode {

    @NotNull
    @Pattern(regexp = "^PAGOPA$")
    private String idCode;

    @NotNull
    @Pattern(regexp = "^002$")
    private String version;

    @NotNull
    @Size(min = 18, max = 18)
    @Pattern(regexp = "^\\d{18}$")
    private String noticeNumber;

    @NotNull
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^\\d{11}$")
    private String paTaxCode;

    @NotNull
    @Size(min = 2, max = 11)
    @Pattern(regexp = "^\\d{2,11}$")
    private String amount;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QrCode{");
        sb.append("idCode='").append(idCode).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", noticeNumber='").append(noticeNumber).append('\'');
        sb.append(", paTaxCode='").append(paTaxCode).append('\'');
        sb.append(", amount='").append(amount).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
