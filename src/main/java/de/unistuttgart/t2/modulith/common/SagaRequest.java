package de.unistuttgart.t2.modulith.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to start a saga.
 * <p>
 * Holds all the information that are necessary for the saga. That is information about the payment method and the
 * costs, and the sessionId to identify the users. all the Data, that any saga participant might possibly
 * <p>
 * Used to communicate with the orchestrator service.
 */
public final class SagaRequest {

    // for payment
    @JsonProperty("cardNumber")
    private final String cardNumber;
    @JsonProperty("cardOwner")
    private final String cardOwner;
    @JsonProperty("checksum")
    private final String checksum;

    // identify user
    @JsonProperty("sessionId")
    private final String sessionId;

    // costs
    @JsonProperty("total")
    private final double total;

    @JsonCreator
    public SagaRequest(String sessionId, String cardNumber, String cardOwner, String checksum, double total) {
        this.cardNumber = cardNumber;
        this.cardOwner = cardOwner;
        this.checksum = checksum;
        this.sessionId = sessionId;
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

    public String getSessionId() {
        return sessionId;
    }

    public double getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return String.format("SessionID : %s, Card : %s, %s, %s , Total : %f", sessionId, cardOwner, cardNumber,
            checksum, total);
    }
}
