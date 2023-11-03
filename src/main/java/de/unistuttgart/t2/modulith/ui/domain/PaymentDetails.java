package de.unistuttgart.t2.modulith.ui.domain;

/**
 * Definition of the JSP model attribute 'details'.
 * It is used to complete an order.
 *
 * @author maumau
 */
public class PaymentDetails {

    private String cardNumber;
    private String cardOwner;
    private String checksum;

    public PaymentDetails() {
    }

    public PaymentDetails(String cardNumber, String cardOwner, String checksum) {
        this.cardNumber = cardNumber;
        this.cardOwner = cardOwner;
        this.checksum = checksum;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardOwner() {
        return cardOwner;
    }

    public void setCardOwner(String cardOwner) {
        this.cardOwner = cardOwner;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        return "PaymentDetails [cardNumber=" + cardNumber + ", cardOwner=" + cardOwner + ", checksum=" + checksum + "]";
    }
}
