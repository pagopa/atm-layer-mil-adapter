/**
 * 
 */
package it.gov.pagopa.miladapter.services.model;

import it.gov.pagopa.miladapter.util.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

public class Preset implements Serializable{

	/**
	 *Preset.java
	 */
	private static final long serialVersionUID = -8846393398844116002L;

	/*
	 * Tax code of the creditor company
	 */
	@NotNull(message = "[" + ErrorCode.PA_TAX_CODE_MUST_NOT_BE_NULL + "] paTaxCode must not be null")
	@Pattern(regexp = "^[0-9]{11}$", message = "[" + ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
	private String paTaxCode;
	
	/*
	 * Subscriber ID
	 */
	@NotNull(message = "[" + ErrorCode.SUBSCRIBER_ID_MUST_NOT_BE_NULL + "] subscriberId must not be null")
	@Pattern(regexp = "^[0-9a-z]{6}$", message = "[" + ErrorCode.SUBSCRIBER_ID_MUST_MATCH_REGEXP + "] subscriberId must match \"{regexp}\"")
	private String subscriberId;
	
	/*
	 * Preset Id
	 */
	@NotNull(message = "[" + ErrorCode.PRESET_ID_MUST_NOT_BE_NULL + "] presetId must not be null")
	@Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$", message = "[" + ErrorCode.PRESET_ID_MUST_MATCH_REGEXP + "] presetId must match \"{regexp}\"")
	private String presetId;
	
	/**
	 * @return the paTaxCode
	 */
	public String getPaTaxCode() {
		return paTaxCode;
	}
	
	/**
	 * @param paTaxCode the paTaxCode to set
	 */
	public void setPaTaxCode(String paTaxCode) {
		this.paTaxCode = paTaxCode;
	}
	
	/**
	 * @return the subscriberId
	 */
	public String getSubscriberId() {
		return subscriberId;
	}
	
	/**
	 * @param subscriberId the subscriberId to set
	 */
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	
	/**
	 * @return the presetId
	 */
	public String getPresetId() {
		return presetId;
	}
	
	/**
	 * @param presetId the presetId to set
	 */
	public void setPresetId(String presetId) {
		this.presetId = presetId;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Preset [paTaxCode=");
		builder.append(paTaxCode);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append(", presetId=");
		builder.append(presetId);
		builder.append("]");
		return builder.toString();
	}
}
