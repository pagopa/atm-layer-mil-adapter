package it.gov.pagopa.miladapter.util;


public final class ErrorCode {
	public static final String MODULE_ID = "008";

	// validation errors
	public static final String ENCODED_QRCODE_MUST_MATCH_REGEXP 							= MODULE_ID + "000001";
	public static final String PA_TAX_CODE_MUST_MATCH_REGEXP 								= MODULE_ID + "000002";
	public static final String NOTICE_NUMBER_MUST_MATCH_REGEXP 								= MODULE_ID + "000003";
	public static final String ERROR_IDEMPOTENCY_KEY_MUST_NOT_BE_NULL 						= MODULE_ID + "000004";
	public static final String ERROR_IDEMPOTENCY_KEY_MUST_MATCH_REGEXP 						= MODULE_ID + "000005";
	public static final String ERROR_AMOUNT_MUST_NOT_BE_NULL								= MODULE_ID + "000006";
	public static final String ERROR_AMOUNT_MUST_BE_GREATER_THAN 							= MODULE_ID + "000007";
	public static final String ERROR_AMOUNT_MUST_BE_LESS_THAN 								= MODULE_ID + "000008";
	public static final String ERROR_OUTCOME_MUST_NOT_BE_NULL								= MODULE_ID + "000009";
	public static final String ERROR_OUTCOME_MUST_MATCH_MATCH_REGEXP 						= MODULE_ID + "00000A";
	public static final String ERROR_PAYMENT_TOKEN_LIST_MUST_NOT_BE_NULL 					= MODULE_ID + "00000B";
	public static final String ERROR_PAYMENT_TOKEN_LIST_MUST_HAVE_AT_MOST 					= MODULE_ID + "00000C";
	public static final String ERROR_PAYMENT_TOKEN_MATCH_MATCH_REGEXP 						= MODULE_ID + "00000D";
	public static final String ERROR_PAYMENT_METHOD_MUST_NOT_BE_NULL 						= MODULE_ID + "00000E";
	public static final String ERROR_PAYMENT_METHOD_MUST_MATCH_REGEXP 						= MODULE_ID + "00000F";
	public static final String ERROR_TRANSACTION_ID_MUST_NOT_BE_NULL 						= MODULE_ID + "000010";
	public static final String ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP 						= MODULE_ID + "000011";
	public static final String ERROR_TOTAL_AMOUNT_MUST_NOT_BE_NULL 							= MODULE_ID + "000012";
	public static final String ERROR_TOTAL_AMOUNT_MUST_BE_GREATER_THAN 						= MODULE_ID + "000013";
	public static final String ERROR_TOTAL_AMOUNT_MUST_BE_LESS_THAN 						= MODULE_ID + "000014";
	public static final String ERROR_FEE_MUST_NOT_BE_NULL 									= MODULE_ID + "000015";
	public static final String ERROR_FEE_MUST_BE_GREATER_THAN 								= MODULE_ID + "000016";
	public static final String ERROR_FEE_MUST_BE_LESS_THAN 									= MODULE_ID + "000017";
	public static final String ERROR_PAYMENT_TIMESTAMP_MUST_NOT_BE_NULL						= MODULE_ID + "000018";
	public static final String ERROR_PAYMENT_TIMESTAMP_MUST_MATCH_REGEXP 					= MODULE_ID + "000019";
	public static final String ERROR_PAYMENT_DATE_MUST_NOT_BE_NULL 							= MODULE_ID + "00001A";
	public static final String ERROR_PAYMENT_DATE_MUST_MATCH_REGEXP 						= MODULE_ID + "00001B";
	public static final String ERROR_PAYMENTS_MUST_NOT_BE_NULL 								= MODULE_ID + "00001C";
	public static final String ERROR_PAYMENTS_MUST_HAVE_AT_MOST_ELEMENTS 					= MODULE_ID + "00001D";
	public static final String ERROR_DESCRIPTION_MUST_MATCH_REGEXP 							= MODULE_ID + "00001E";
	public static final String ERROR_CREDITOR_REFERENCE_ID_MUST_MATCH_REGEXP 				= MODULE_ID + "00001F";
	public static final String ERROR_FISCAL_CODE_MUST_MATCH_REGEXP 							= MODULE_ID + "000020";
	public static final String ERROR_COMPANY_MUST_MATCH_REGEXP 								= MODULE_ID + "000021";
	public static final String ERROR_OFFICE_MUST_MATCH_REGEXP 								= MODULE_ID + "000022";
	public static final String ERROR_DEBTOR_MUST_MATCH_REGEXP 								= MODULE_ID + "000023";


	// integration errors - mongodb
	public static final String UNKNOWN_ACQUIRER_ID			 								= MODULE_ID + "000024";
	public static final String ERROR_RETRIEVING_DATA_FROM_DB	 							= MODULE_ID + "000025";


	// integration errors - redis
	public static final String ERROR_RETRIEVING_DATA_FROM_REDIS 							= MODULE_ID + "000025";
	public static final String ERROR_STORING_DATA_INTO_REDIS 								= MODULE_ID + "000026";
	public static final String UNKNOWN_PAYMENT_TRANSACTION 									= MODULE_ID + "000027";


	// integration error node - soap client
	public static final String ERROR_CALLING_NODE_SOAP_SERVICES 							= MODULE_ID + "000028";


	// integration error node - rest client
	public static final String ERROR_CALLING_NODE_REST_SERVICES								= MODULE_ID + "000029";
	public static final String ERROR_CALLING_MIL_REST_SERVICES								= MODULE_ID + "00002A";

	public static final String QRCODE_FORMAT_IS_NOT_VALID									= MODULE_ID + "00002B";

	public static final String ACTIVATE_REQUEST_MUST_NOT_BE_EMPTY 							= MODULE_ID + "00002C";
	public static final String CLOSE_REQUEST_MUST_NOT_BE_EMPTY 								= MODULE_ID + "00002D";

	public static final String PRE_CLOSE_REQUEST_MUST_NOT_BE_EMPTY 							= MODULE_ID + "00002E";

	public static final String ERROR_STORING_DATA_IN_DB 									= MODULE_ID + "00002F";

	public static final String ERROR_UPDATING_DATA_IN_DB 									= MODULE_ID + "000030";

	public static final String PAYMENT_TRANSACTION_ALREADY_EXISTS 							= MODULE_ID + "000031";

	public static final String CACHED_NOTICE_NOT_FOUND			 							= MODULE_ID + "000032";

	public static final String ERROR_TOTAL_AMOUNT_MUST_MATCH_TOTAL_CACHED_VALUE				= MODULE_ID + "000033";
	
	public static final String PA_TAX_CODE_MUST_NOT_BE_NULL									 = MODULE_ID + "000034";
	
	public static final String SUBSCRIBER_ID_MUST_NOT_BE_NULL 								= MODULE_ID + "000035";
	public static final String SUBSCRIBER_ID_MUST_MATCH_REGEXP 								= MODULE_ID + "000036";
	
	public static final String PRESET_ID_MUST_NOT_BE_NULL									= MODULE_ID + "000037";
	public static final String PRESET_ID_MUST_MATCH_REGEXP 									= MODULE_ID + "000038";

	public static final String AUTHENTICATION_ERROR											= MODULE_ID + "000039";
	public static final String ERROR_CALLING_AZUREAD_REST_SERVICES 							= MODULE_ID + "000040";
	public static final String AZUREAD_ACCESS_TOKEN_IS_NULL                     			= MODULE_ID + "000041";

	private ErrorCode() {
	}
}
