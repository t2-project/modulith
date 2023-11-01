package de.unistuttgart.t2.modulith.cart.web;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Request to update the content of a users cart.
 * <p>
 * An update may increase or decrease the number of items in the cart, depending on whether the values in the
 * difference/content map are positive or negative. A negative number implies a decrease of the units of the given
 * product in the cart and a positive number implies an increase thereof.
 * <p>
 */
public final class UpdateCartRequest {

    @JsonProperty("content")
    private Map<String, Integer> difference;

    // Default no-argument constructor
    public UpdateCartRequest() {
    }

    public UpdateCartRequest(Map<String, Integer> content) {
        difference = content;
    }

    @JsonAnySetter
    public void addContent(String key, Integer value) {
        if (difference == null) {
            difference = new HashMap<>();
        }
        difference.put(key, value);
    }

    public void setContent(Map<String, Integer> content) {
        difference = content;
    }

    public Map<String, Integer> getContent() {
        return difference;
    }

    /**
     * Get the productIds of the products in this cart.
     *
     * @return ids of all products in the cart
     */
    @JsonIgnore
    public Collection<String> getProductIds() {
        return difference.keySet();
    }

    /**
     * Get the number of units of a product with the given id or zero if the product is not in the cart.
     *
     * @param productId to identify the product
     * @return number of units if the product is in the cart, zero otherwise
     */
    @JsonIgnore
    public int getUnits(String productId) {
        return difference.getOrDefault(productId, 0);
    }
}
