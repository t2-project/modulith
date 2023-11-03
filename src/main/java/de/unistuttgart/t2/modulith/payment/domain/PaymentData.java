package de.unistuttgart.t2.modulith.payment.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * All information that must be sent to the payment provider. This includes information about the credit cart and the
 * total amount of money to be paid.
 *
 * @author maumau
 */
public class PaymentData {

    @JsonProperty("cardNumber")
    private String cardNumber;
    @JsonProperty("cardOwner")
    private String cardOwner;
    @JsonProperty("checksum")
    private String checksum;
    @JsonProperty("total")
    private double total;

    public PaymentData() {
    }

    @JsonCreator
    public PaymentData(String cardNumber, String cardOwner, String checksum, double total) {
        this.cardNumber = cardNumber;
        this.cardOwner = cardOwner;
        this.checksum = checksum;
        this.total = total;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardOwner() {
        return cardOwner;
    }

    public String getChecksum() {
        return checksum;
    }
}
