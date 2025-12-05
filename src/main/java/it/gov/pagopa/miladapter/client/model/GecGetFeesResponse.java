package it.gov.pagopa.miladapter.client.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GecGetFeesResponse {
	private Boolean belowThreshold;
	private List<BundleOption> bundleOptions;

	@Override
	public String toString() {
		return new StringBuilder("GecGetFeesResponse [belowThreshold=")
			.append(belowThreshold)
			.append(", bundleOptions=")
			.append(bundleOptions)
			.append("]").toString();
	}
}
