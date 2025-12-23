package it.gov.pagopa.miladapter.services.model;

public final class ErrorCode {
	public static final String MODULE_ID = "000";

	/*
	 * Error codes.
	 */
	public static final String REQUEST_ID_MUST_NOT_BE_NULL = MODULE_ID + "000001";
	public static final String REQUEST_ID_MUST_MATCH_REGEXP = MODULE_ID + "000002";

	public static final String VERSION_SIZE_MUST_BE_AT_MOST_MAX = MODULE_ID + "000003";
	public static final String VERSION_MUST_MATCH_REGEXP = MODULE_ID + "000004";

	public static final String ACQUIRER_ID_MUST_NOT_BE_NULL = MODULE_ID + "000005";
	public static final String ACQUIRER_ID_MUST_MATCH_REGEXP = MODULE_ID + "000006";

	public static final String CHANNEL_MUST_NOT_BE_NULL = MODULE_ID + "000007";
	public static final String CHANNEL_MUST_MATCH_REGEXP = MODULE_ID + "000008";

	public static final String TERMINAL_ID_MUST_NOT_BE_NULL = MODULE_ID + "000009";
	public static final String TERMINAL_ID_MUST_MATCH_REGEXP = MODULE_ID + "00000A";

	public static final String MERCHANT_ID_MUST_NOT_BE_NULL_FOR_POS = MODULE_ID + "00000B";
	public static final String MERCHANT_ID_MUST_MATCH_REGEXP = MODULE_ID + "00000C";

	/*
	 * Error descriptions
	 */
	public static final String REQUEST_ID_MUST_NOT_BE_NULL_DESCR = "RequestId must not be null";
	public static final String REQUEST_ID_MUST_MATCH_REGEXP_DESCR = "RequestId must match \"{regexp}\"";

	public static final String VERSION_SIZE_MUST_BE_AT_MOST_MAX_DESCR = "Version size must be at most {max}";
	public static final String VERSION_MUST_MATCH_REGEXP_DESCR = "Version must match \"{regexp}\"";

	public static final String ACQUIRER_ID_MUST_NOT_BE_NULL_DESCR = "AcquirerId must not be null";
	public static final String ACQUIRER_ID_MUST_MATCH_REGEXP_DESCR = "AcquirerId must match \"{regexp}\"";

	public static final String CHANNEL_MUST_NOT_BE_NULL_DESCR = "Channel must not be null";
	public static final String CHANNEL_MUST_MATCH_REGEXP_DESCR = "Channel must match \"{regexp}\"";

	public static final String TERMINAL_ID_MUST_NOT_BE_NULL_DESCR = "TerminalId must not be null";
	public static final String TERMINAL_ID_MUST_MATCH_REGEXP_DESCR = "TerminalId must match \"{regexp}\"";

	public static final String MERCHANT_ID_MUST_NOT_BE_NULL_FOR_POS_DESCR = "If Channel equals to POS, MerchantId must not be null";
	public static final String MERCHANT_ID_MUST_MATCH_REGEXP_DESCR = "MerchantId must match \"{regexp}\"";

	/*
	 * Error messages
	 */
	public static final String REQUEST_ID_MUST_NOT_BE_NULL_MSG = "[" + REQUEST_ID_MUST_NOT_BE_NULL + "] " + REQUEST_ID_MUST_NOT_BE_NULL_DESCR;
	public static final String REQUEST_ID_MUST_MATCH_REGEXP_MSG = "[" + REQUEST_ID_MUST_MATCH_REGEXP + "] " + REQUEST_ID_MUST_MATCH_REGEXP_DESCR;

	public static final String VERSION_SIZE_MUST_BE_AT_MOST_MAX_MSG = "[" + VERSION_SIZE_MUST_BE_AT_MOST_MAX + "] " + VERSION_SIZE_MUST_BE_AT_MOST_MAX_DESCR;
	public static final String VERSION_MUST_MATCH_REGEXP_MSG = "[" + VERSION_MUST_MATCH_REGEXP + "] " + VERSION_MUST_MATCH_REGEXP_DESCR;

	public static final String ACQUIRER_ID_MUST_NOT_BE_NULL_MSG = "[" + ACQUIRER_ID_MUST_NOT_BE_NULL + "] " + ACQUIRER_ID_MUST_NOT_BE_NULL_DESCR;
	public static final String ACQUIRER_ID_MUST_MATCH_REGEXP_MSG = "[" + ACQUIRER_ID_MUST_MATCH_REGEXP + "] " + ACQUIRER_ID_MUST_MATCH_REGEXP_DESCR;

	public static final String CHANNEL_MUST_NOT_BE_NULL_MSG = "[" + CHANNEL_MUST_NOT_BE_NULL + "] " + CHANNEL_MUST_NOT_BE_NULL_DESCR;
	public static final String CHANNEL_MUST_MATCH_REGEXP_MSG = "[" + CHANNEL_MUST_MATCH_REGEXP + "] " + CHANNEL_MUST_MATCH_REGEXP_DESCR;

	public static final String TERMINAL_ID_MUST_NOT_BE_NULL_MSG = "[" + TERMINAL_ID_MUST_NOT_BE_NULL + "] " + TERMINAL_ID_MUST_NOT_BE_NULL_DESCR;
	public static final String TERMINAL_ID_MUST_MATCH_REGEXP_MSG = "[" + TERMINAL_ID_MUST_MATCH_REGEXP + "] " + TERMINAL_ID_MUST_MATCH_REGEXP_DESCR;

	public static final String MERCHANT_ID_MUST_NOT_BE_NULL_FOR_POS_MSG = "[" + MERCHANT_ID_MUST_NOT_BE_NULL_FOR_POS + "] " + MERCHANT_ID_MUST_NOT_BE_NULL_FOR_POS_DESCR;
	public static final String MERCHANT_ID_MUST_MATCH_REGEXP_MSG = "[" + MERCHANT_ID_MUST_MATCH_REGEXP + "] " + MERCHANT_ID_MUST_MATCH_REGEXP_DESCR;

	private ErrorCode() {
	}
}
