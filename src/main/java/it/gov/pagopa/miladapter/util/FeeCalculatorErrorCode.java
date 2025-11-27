/*
 * ErrorCode.java
 *
 * 12 dec 2022
 */

package it.gov.pagopa.miladapter.util;

public final class FeeCalculatorErrorCode {
	public static final String MODULE_ID = "007";

	public static final String PAYMENT_METHOD_MUST_MATCH_REGEXP = MODULE_ID + "000001";

	public static final String NOTICES_MUST_NOT_BE_NULL = MODULE_ID + "000003";
	public static final String NOTICES_LIST_EXCEEDED_SIZE = MODULE_ID + "000004";

	public static final String AMOUNT_MUST_NOT_BE_NULL = MODULE_ID + "000005";
	public static final String AMOUNT_EXCEEDED_MIN_VALUE = MODULE_ID + "000006";
	public static final String AMOUNT_EXCEED_MAX_VALUE = MODULE_ID + "000007";

	public static final String PA_TAX_CODE_MUST_MATCH_REGEXP = MODULE_ID + "000008";
	public static final String PA_TAX_CODE_MUST_NOT_BE_NULL = MODULE_ID + "000009";

	public static final String TRANSFERS_MUST_NOT_BE_NULL = MODULE_ID + "00000A";
	public static final String TRANSFERS_EXCEEDED_MAX_VALUE = MODULE_ID + "00000B";

	public static final String ERROR_RETRIEVING_FEES = MODULE_ID + "00000C";

	public static final String TRANSFERS_PA_TAX_CODE_MUST_NOT_BE_NULL = MODULE_ID + "00000E";
	public static final String TRANSFERS_PA_TAX_CODE_MUST_MATCH_REGEXP = MODULE_ID + "00000F";

	public static final String TRANSFERS_CATEGORY_MUST_NOT_BE_NULL = MODULE_ID + "000010";
	public static final String TRANSFERS_CATEGORY_MUST_MATCH_REGEXP = MODULE_ID + "000011";

	public static final String NO_FEE_FOUND = MODULE_ID + "000012";

	public static final String UNKNOWN_ACQUIRER_ID = MODULE_ID + "000013";
	public static final String ERROR_CALLING_MIL_REST_SERVICES = MODULE_ID + "000013";

	public static final String REQUEST_MUST_NOT_BE_EMPTY = MODULE_ID + "000014";

	public static final String AUTHENTICATION_ERROR = MODULE_ID + "000015";
	public static final String ERROR_CALLING_AZUREAD_REST_SERVICES = MODULE_ID + "000016";
	public static final String AZUREAD_ACCESS_TOKEN_IS_NULL = MODULE_ID + "000017";

	private FeeCalculatorErrorCode() {
		// This class cannot be instantiated!
	}
}
