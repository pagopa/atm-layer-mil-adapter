package it.gov.pagopa.miladapter.client.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BundleOption {
	private String abi;
	private String bundleDescription;
	private String bundleName;
    private String idBrokerPsp;
	private String idBundle;
	private String idChannel;
	private String idCiBundle;
	private String idPsp;
	private Boolean onUs;
	private String paymentMethod;
	private long primaryCiIncurredFee;
	private long taxPayerFee;
	private String touchpoint;

	@Override
	public String toString() {
		return new StringBuilder("BundleOption [abi=")
			.append(abi)
			.append(", bundleDescription=")
			.append(bundleDescription)
			.append(", bundleName=")
			.append(bundleName)
			.append(", idBrokerPsp=")
			.append(idBrokerPsp)
			.append(", idBundle=")
			.append(idBundle)
			.append(", idChannel=")
			.append(idChannel)
			.append(", idCiBundle=")
			.append(idCiBundle)
			.append(", idPsp=")
			.append(idPsp)
			.append(", onUs=")
			.append(onUs)
			.append(", paymentMethod=")
			.append(paymentMethod)
			.append(", primaryCiIncurredFee=")
			.append(primaryCiIncurredFee)
			.append(", taxPayerFee=")
			.append(taxPayerFee)
			.append(", touchpoint=")
			.append(touchpoint)
			.append("]").toString();
	}
}
