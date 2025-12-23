package it.gov.pagopa.miladapter.util;

/**
 * Class containing common regex used by the REST endpoint to validate path params
 */
public class PaymentNoticeConstants {

	/**
	 * The regex to be used to validate the base64url encoded QR code
	 */
	public static final String ENCODED_QRCODE_REGEX = "^[a-zA-Z0-9_-]{32,128}$";

	/**
	 * The regex to be used to validate the QR code
	 */
	public static final String QRCODE_REGEX 		= "^[ -~]{6}\\|[ -~]{3}\\|\\d{18}\\|\\d{11}\\|\\d{2,11}$";

	/**
	 * The regex to be used to validate the transaction ID
	 */
	public static final String TRANSACTION_ID_REGEX = "^[a-zA-Z0-9]{32}$";

	/**
	 * The regex to be used to validate the PA tax code
	 */
	public static final String PA_TAX_CODE_REGEX 	= "^\\d{11}$";

	/**
	 * The regex to be used to validate the notice number
	 */
	public static final String NOTICE_NUMBER_REGEX	= "^\\d{18}$";

	private PaymentNoticeConstants() {}
}
