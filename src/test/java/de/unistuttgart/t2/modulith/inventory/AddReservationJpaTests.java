package de.unistuttgart.t2.modulith.inventory;

import de.unistuttgart.t2.modulith.inventory.repository.Reservation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the adding of reservations.
 *
 * @author maumau
 */
@DataJpaTest
@ExtendWith(SpringExtension.class)
@Import(InventoryService.class)
@ActiveProfiles("test")
public class AddReservationJpaTests extends RepositoryTests {

    @Test
    public void makeNewReservation(@Autowired InventoryService inventoryService) {
        // make reservation
        String key = "newSessionId";
        inventoryService.makeReservation(id1, key, 1);

        // assert things
        assertEquals(2, productRepository.count());

        List<Reservation> actualReservations = productRepository.findById(id1).get().getReservations();

        assertEquals(4, actualReservations.size());

        Reservation actualReservation = getReservation(actualReservations, key);

        assertEquals(1, actualReservation.getUnits());
    }

    @Test
    public void makeNoNewReservationIfUnitsAreZero(@Autowired InventoryService inventoryService) {
        // make reservation
        String key = "newSessionId";
        inventoryService.makeReservation(id1, key, 0);

        // assert things
        assertEquals(2, productRepository.count());

        List<Reservation> actual = productRepository.findById(id1).get().getReservations();

        assertEquals(3, actual.size());
        actual = actual.stream().filter(r -> r.getUserId().equals(key)).collect(Collectors.toList());
        assertTrue(actual.isEmpty());
    }

    @Test
    public void increaseReservation(@Autowired InventoryService inventoryService) {
        // make reservation
        String key = existingSession1;
        inventoryService.makeReservation(id1, key, 1);

        // assert things
        assertEquals(2, productRepository.count());

        List<Reservation> actual = productRepository.findById(id1).get().getReservations();

        assertEquals(3, actual.size());
        actual = actual.stream().filter(r -> r.getUserId().equals(key)).collect(Collectors.toList());

        assertEquals(1, actual.size());
        assertEquals(2, actual.get(0).getUnits());
    }

    @Test
    public void reservationIsUnchangedIfUnitsOfNewReservationAreZero(@Autowired InventoryService inventoryService) {
        // make reservation
        String key = existingSession1;
        inventoryService.makeReservation(id1, key, 0);

        // assert things
        assertEquals(2, productRepository.count());

        List<Reservation> actual = productRepository.findById(id1).get().getReservations();

        assertEquals(3, actual.size());
        actual = actual.stream().filter(r -> r.getUserId().equals(key)).collect(Collectors.toList());

        assertEquals(1, actual.size());
        assertEquals(1, actual.get(0).getUnits());
    }

    @Test
    public void throwIAEProductIDReservation(@Autowired InventoryService inventoryService) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.makeReservation(null, existingSession1, 1);
        });
    }

    @Test
    public void throwIAESessionIDReservation(@Autowired InventoryService inventoryService) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.makeReservation(id1, null, 1);
        });
    }

    @Test
    public void throwIAENegativeUnitsReservation(@Autowired InventoryService inventoryService) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.makeReservation(id1, existingSession1, -1);
        });
    }

    @Test
    public void throwIAEUnitsReservation(@Autowired InventoryService inventoryService) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.makeReservation(id1, existingSession1, 1000);
        });
    }

    @Test
    public void throwNSEEReservation(@Autowired InventoryService inventoryService) {
        Assertions.assertThrows(NoSuchElementException.class, () -> {
            inventoryService.makeReservation("wrongid", existingSession1, 1);
        });
    }
}
