/**
 *
 */
package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.model.QrCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QrCodeParserTest {

	private QrCodeParser qrCodeParser;
	private Validator validator;

	@BeforeAll
	void setup() {
		qrCodeParser = new QrCodeParser();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void testParse() {

		QrCode parsedQrCode = qrCodeParser.parse(PaymentTestData.QR_CODE);

		Assertions.assertEquals("PAGOPA", parsedQrCode.getIdCode());
		Assertions.assertEquals("002", parsedQrCode.getVersion());
		Assertions.assertEquals(PaymentTestData.NOTICE_NUMBER, parsedQrCode.getNoticeNumber());
		Assertions.assertEquals(PaymentTestData.PA_TAX_CODE, parsedQrCode.getPaTaxCode());
		Assertions.assertEquals("9999", parsedQrCode.getAmount());

	}

	@Test
	void testB64UrlParse_OK_withPadding() {

		byte[] bytes = Base64.getUrlEncoder().encode(PaymentTestData.QR_CODE.getBytes(StandardCharsets.UTF_8));
		QrCode parsedQrCode = qrCodeParser.b64UrlParse(new String(bytes, StandardCharsets.UTF_8));

		Assertions.assertEquals("PAGOPA", parsedQrCode.getIdCode());
		Assertions.assertEquals("002", parsedQrCode.getVersion());
		Assertions.assertEquals(PaymentTestData.NOTICE_NUMBER, parsedQrCode.getNoticeNumber());
		Assertions.assertEquals(PaymentTestData.PA_TAX_CODE, parsedQrCode.getPaTaxCode());
		Assertions.assertEquals("9999", parsedQrCode.getAmount());

	}

	@Test
	void testB64UrlParse_OK_withoutPadding() {

		byte[] bytes = Base64.getUrlEncoder().withoutPadding().encode(PaymentTestData.QR_CODE.getBytes(StandardCharsets.UTF_8));
		QrCode parsedQrCode = qrCodeParser.b64UrlParse(new String(bytes, StandardCharsets.UTF_8));

		Assertions.assertEquals("PAGOPA", parsedQrCode.getIdCode());
		Assertions.assertEquals("002", parsedQrCode.getVersion());
		Assertions.assertEquals(PaymentTestData.NOTICE_NUMBER, parsedQrCode.getNoticeNumber());
		Assertions.assertEquals(PaymentTestData.PA_TAX_CODE, parsedQrCode.getPaTaxCode());
		Assertions.assertEquals("9999", parsedQrCode.getAmount());

	}

	@Test
	void testB64UrlParse_KO() {

		String encodedQrCode = Base64.getUrlEncoder().encodeToString("https://www.test.com".getBytes(StandardCharsets.UTF_8));

		// Manual validation since we're not using Spring context
		Assertions.assertThrows(Exception.class, () -> {
			String invalidQrCode = new String(Base64.getUrlDecoder().decode(encodedQrCode), StandardCharsets.UTF_8);
			// Validate manually
			Set<ConstraintViolation<QrCodeParser>> violations = validator.forExecutables()
					.validateParameters(qrCodeParser,
							QrCodeParser.class.getMethod("parse", String.class),
							new Object[]{invalidQrCode});

			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}
			qrCodeParser.parse(invalidQrCode);
		});

	}
}
