package it.gov.pagopa.miladapter.services.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class Errors {
	/*
	 * List of error codes
	 */
	private List<String> errors;

	/*
	 * List of error descriptions.
	 */
	private List<String> descriptions;

	public Errors(List<String> errors) {
		this.errors = errors;
	}

	public Errors(String error, String description) {
		errors = List.of(error);
		descriptions = List.of(description);
	}
}
