package de.unistuttgart.t2.modulith.inventory.exceptions;

import java.io.Serial;

public class InsufficientUnitsAvailableException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public InsufficientUnitsAvailableException(String productId, int unitsToReserve, int unitsAvailable) {
        super(String.format("Insufficient units available for product %s. Tried to reserve %d units, but only %d are available.",
            productId, unitsToReserve, unitsAvailable));
    }
}
