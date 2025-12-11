package it.gov.pagopa.miladapter.services.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity bean containing the data of a payment notice
 */
@Getter
@Setter
@ToString
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
}
