package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.model.QrCode;
import jakarta.validation.constraints.Pattern;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class QrCodeParser {

    /**
     * Creates a QrCode instance from its string representation
     * @param qrCode the string representation of the QR code
     * @return the {@link QrCode} instance
     */
    public QrCode parse(
            @Pattern(regexp = PaymentNoticeConstants.QRCODE_REGEX,
            message = "[" + ErrorCode.QRCODE_FORMAT_IS_NOT_VALID + "] qrCode must match \"{regexp}\"") String qrCode) {

        QrCode instance = new QrCode();
        String[] parts = StringUtils.split(qrCode, "|");
        instance.setIdCode(ArrayUtils.get(parts, 0, null));
        instance.setVersion(ArrayUtils.get(parts, 1, null));
        instance.setNoticeNumber(ArrayUtils.get(parts, 2, null));
        instance.setPaTaxCode(ArrayUtils.get(parts, 3, null));
        instance.setAmount(ArrayUtils.get(parts, 4, null));

        return instance;
    }

    /**
     * Creates a QrCode instance from its string representation, encoded in base64url
     * @param b64UrlQrCode the base64Url encoded string representation of the QR code
     * @return the {@link QrCode} instance
     */
    public QrCode b64UrlParse(String b64UrlQrCode) {
        byte[] bytes = Base64.getUrlDecoder().decode(b64UrlQrCode.getBytes(StandardCharsets.UTF_8));
        String qrCode = new String(bytes, StandardCharsets.UTF_8);

        return parse(qrCode);
    }
}
