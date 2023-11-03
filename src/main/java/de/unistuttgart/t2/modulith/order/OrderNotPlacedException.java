package de.unistuttgart.t2.modulith.order;

import java.io.Serial;

/**
 * Indicates that the placement of an order failed.
 *
 * @author maumau
 */
public final class OrderNotPlacedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public OrderNotPlacedException(String message) {
        super(message);
    }

    public OrderNotPlacedException(String message, Exception e) {
        super(message, e);
    }
}
