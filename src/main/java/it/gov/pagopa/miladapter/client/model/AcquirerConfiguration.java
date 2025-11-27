package it.gov.pagopa.miladapter.client.model;

import it.gov.pagopa.miladapter.model.PspConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * Class containing the acquirer configurations needed to integrate with the node services
 */
@Getter
@Setter
public class AcquirerConfiguration {

	/**
	 * The psp configuration to be used when calling the "verify" and "activate" APIs of the node
	 */
	private PspConfiguration pspConfigForVerifyAndActivate;


	/**
	 * The psp configuration to be used when calling the "close" API of the node and the "getFee" api of GEC
	 */
	private PspConfiguration pspConfigForGetFeeAndClosePayment;


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PspConfiguration{");
		sb.append("pspConfigForVerifyAndActivate=").append(pspConfigForVerifyAndActivate);
		sb.append(", pspConfigForGetFeeAndClosePayment=").append(pspConfigForGetFeeAndClosePayment);
		sb.append('}');
		return sb.toString();
	}
}
