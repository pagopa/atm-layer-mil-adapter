package it.gov.pagopa.miladapter.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Helper formatter for the StAmount element of the node wsdl.
 * The CXF maven plugin does not create a correct formatter respecting the pattern (2 decimal digits),
 * this formatter will force the format when serializing/deserializing the soap xml request
 *
 * <xsd:simpleType name="stAmount">
 * 		<xsd:restriction base="xsd:decimal">
 * 			<xsd:pattern value="\d+\.\d{2}" />
 * 			<xsd:maxInclusive value="999999999.99" />
 * 		</xsd:restriction>
 * 	</xsd:simpleType>
 *
 * 	@see <a href="https://github.com/pagopa/pagopa-api/blob/SANP3.2.0/xsd/sac-common-types-1.0.xsd">sac-common-types-1.0.xsd</a>
 */
public class StAmountFormatter {

    // private constructor to avoid instantiation
    private StAmountFormatter() {}

    /**
     * Serialize a BigDecimal forcing always 2 decimal digits
     * @param value the BigDecimal to serialize
     * @return the String representation of the BigDecimal
     */
    public static String printBigDecimal(BigDecimal value) {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');

        DecimalFormat df = new DecimalFormat("0.00");
        df.setDecimalFormatSymbols(symbols);
        df.setGroupingUsed(false);

        return df.format(value);
    }

    /**
     * Deserialize a BigDecimal
     * @param value the string representation of the BigDecimal
     * @return the BigDecimal instance
     */
    public static BigDecimal parseBigDecimal(String value) {
        return new BigDecimal(value);
    }

}
