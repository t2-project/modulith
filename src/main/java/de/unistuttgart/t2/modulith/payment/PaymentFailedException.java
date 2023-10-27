package de.unistuttgart.t2.modulith.payment;

import java.io.Serial;

/**
 * Indicates that the execution of a payment failed.
 *
 * @author maumau
 */
public final class PaymentFailedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PaymentFailedException(String message) {
        super(message);
    }

    public PaymentFailedException(String message, Exception e) {
        super(message, e);
    }
}
