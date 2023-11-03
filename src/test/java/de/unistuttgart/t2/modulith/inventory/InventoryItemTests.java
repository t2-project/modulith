package de.unistuttgart.t2.modulith.inventory;

import de.unistuttgart.t2.modulith.inventory.repository.InventoryItem;
import de.unistuttgart.t2.modulith.inventory.repository.Reservation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryItemTests {

    private InventoryItem item;
    String existingSession1 = "session1";
    String existingSession2 = "session2";
    String existingSession3 = "session3";

    @BeforeEach
    void setUp() {
        item = new InventoryItem("id", "name", "description", 15, 0.5);
        item.addReservation(existingSession1, 1);
        item.addReservation(existingSession2, 2);
        item.addReservation(existingSession3, 3);

    }

    @Test
    public void reservationsOfNewInventoryItemAreNeverNull() {
        assertNotNull(new InventoryItem().getReservations());
        assertNotNull(new InventoryItem("", "", "", 0, 0).getReservations());
    }

    @Test
    public void availableUnitsIsCorrect() {
        assertEquals(9, item.getAvailableUnits());
    }

    @Test
    public void availableUnitsAreInsufficientForNewReservation_Exception() {
        item = new InventoryItem("id", "name", "description", 0, 0.5, List.of(new Reservation(3, "foo", item)));

        Assertions.assertThrows(IllegalStateException.class, () -> {
            item.getAvailableUnits();
        });
    }

    @Test
    public void addReservationForNewSession() {
        item.addReservation("newSession", 2);
        assertEquals(4, item.getReservations().size());
        assertEquals(7, item.getAvailableUnits());
    }

    @Test
    public void updateReservationOfExistingSession() {
        item.addReservation(existingSession3, 2);
        assertEquals(3, item.getReservations().size());
        assertEquals(7, item.getAvailableUnits());
    }

    @Test
    public void addReservationWithZeroUnitsForNewSession_StateUnchanged() {
        item.addReservation("newSession", 0);
        assertEquals(3, item.getReservations().size());
        assertEquals(9, item.getAvailableUnits());
    }

    @Test
    public void addReservationWithTooMuchUnits_exceptionTooMuchUnits_stateUnchanged() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            item.addReservation("newSession", 400);
        });
        assertEquals(3, item.getReservations().size());
        assertEquals(9, item.getAvailableUnits());
    }

    @Test
    public void addReservationWithNegativeUnits_exceptionNegativeUnits_stateUnchanged() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            item.addReservation("newSession", -4);
        });
        assertEquals(3, item.getReservations().size());
        assertEquals(9, item.getAvailableUnits());
    }

    @Test
    public void commitReservation_fair() {
        item.commitReservation(existingSession1);
        assertEquals(2, item.getReservations().size());
        assertEquals(9, item.getAvailableUnits()); // unchanged
        assertEquals(14, item.getUnits()); // changed
    }

    @Test
    public void commitReservationUnknownId_UnchangedInventoryItem() {
        item.commitReservation("newSession");
        assertEquals(3, item.getReservations().size());
        assertEquals(9, item.getAvailableUnits()); // unchanged
        assertEquals(15, item.getUnits()); // unchanged
    }

    @Test
    public void equalsInventoryItem() {
        assertEquals(item, item);

        InventoryItem other = new InventoryItem("id", "name", "description", 15, 0.5);
        other.addReservation(existingSession1, 1);
        other.addReservation(existingSession2, 2);
        other.addReservation(existingSession3, 3);

        assertEquals(item, other);
        assertEquals(other, item);

    }
}
