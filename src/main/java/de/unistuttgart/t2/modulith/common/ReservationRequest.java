package de.unistuttgart.t2.modulith.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to place a reservation on a product.
 * <p>
 * This request if for reservation of single product. If you want to place reservations on multiple products you have to
 * request one after another.
 * <p>
 * Used to communicate with the inventory service.
 *
 * @author maumau
 */
public final class ReservationRequest {

    @JsonProperty("productId")
    private final String productId;
    @JsonProperty("sessionId")
    private final String sessionId;
    @JsonProperty("units")
    private final int units;

    @JsonCreator
    public ReservationRequest(String productId, String sessionId, int units) {
        this.productId = productId;
        this.sessionId = sessionId;
        this.units = units;
    }

    public String getProductId() {
        return productId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getUnits() {
        return units;
    }
}
