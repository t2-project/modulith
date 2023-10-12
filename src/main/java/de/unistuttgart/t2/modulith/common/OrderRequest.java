package de.unistuttgart.t2.modulith.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to order all Items in a users cart.
 * <p>
 * Holds the payment information and the seesionId to identify the user's cart . The products to be ordered will be
 * retrieved from the cart service, and the sessionId is in that session object that is always there.
 * <p>
 * Used to communicate from UI to UIBackend.
 *
 * @author maumau
 */
public final class OrderRequest {

    @JsonProperty("cardNumber")
    private final String cardNumber;
    @JsonProperty("cardOwner")
    private final String cardOwner;
    @JsonProperty("checksum")
    private final String checksum;
    @JsonProperty("sessionId")
    private final String sessionId;

    @JsonCreator
    public OrderRequest(String cardNumber, String cardOwner, String checksum, String sessionId) {
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
