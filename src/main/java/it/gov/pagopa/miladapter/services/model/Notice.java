package it.gov.pagopa.miladapter.services.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity bean containing the data of a payment notice
 */
@Getter
@Setter
public class Notice {

    /**
     * The payment token returned by the node
     */
    private String paymentToken;

    /**
     * The tax code of the public administration
     */
    private String paTaxCode;

    /**
     * The identifier of the payment notice
     */
    private String noticeNumber;

    /**
     * The amount of the payment notice
     */
    private Long amount;

    /**
     * The description of the payment notice
     */
    private String description;

    /**
     * The company that issued the payment notice
     */
    private String company;

    /**
     * The office of the company that issued the payment notice
     */
    private String office;

    /**
     * The creditor identifier, as returned by the callback of the node after a successful payment
     */
    private String creditorReferenceId;

    /**
     * The debtor name, as returned by the callback of the node after a successful payment
     */
    private String debtor;

    /**
     * Transfer essential data
     */
    private List<Transfer> transfers;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Notice{");
        sb.append("paymentToken='").append(paymentToken).append('\'');
        sb.append(", paTaxCode='").append(paTaxCode).append('\'');
        sb.append(", noticeNumber='").append(noticeNumber).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", description='").append(description).append('\'');
        sb.append(", company='").append(company).append('\'');
        sb.append(", office='").append(office).append('\'');
        sb.append(", creditorReferenceId='").append(creditorReferenceId).append('\'');
        sb.append(", debtor='").append(debtor).append('\'');
        sb.append(", transfers=").append(transfers);
        sb.append('}');
        return sb.toString();
    }
}
