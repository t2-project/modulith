package de.unistuttgart.t2.modulith.uibackend.web;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to order all Items in a users cart.
 * <p>
 * Holds the payment information and the sessionId to identify the user's cart. The products to be ordered will be
 * retrieved from the cart module, and the sessionId is in that session object that is always there.
 *
 * @author maumau
 */
public final class OrderRequest {

    @JsonProperty("cardNumber")
    private String cardNumber;
    @JsonProperty("cardOwner")
    private String cardOwner;
    @JsonProperty("checksum")
    private String checksum;
    @JsonProperty("sessionId")
    private String sessionId;

    // Default no-argument constructor
    public OrderRequest() {
    }

    public OrderRequest(String cardNumber, String cardOwner, String checksum, String sessionId) {
        this.cardNumber = cardNumber;
        this.cardOwner = cardOwner;
        this.checksum = checksum;
        this.sessionId = sessionId;
    }

    @JsonAnySetter
    public void update(String cardNumber, String cardOwner, String checksum, String sessionId) {
        this.cardNumber = cardNumber;
        this.cardOwner = cardOwner;
        this.checksum = checksum;
        this.sessionId = sessionId;
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

    public String getSessionId() {
        return sessionId;
    }
}
