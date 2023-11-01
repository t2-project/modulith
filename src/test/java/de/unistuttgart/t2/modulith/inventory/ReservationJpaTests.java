package de.unistuttgart.t2.modulith.inventory;

import de.unistuttgart.t2.modulith.inventory.repository.InventoryItem;
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
 * Test the adding, committing and deleting of reservations.
 *
 * @author maumau
 */
@DataJpaTest
@ExtendWith(SpringExtension.class)
@Import(InventoryService.class)
@ActiveProfiles("test")
public class ReservationJpaTests extends BaseRepositoryTest {

    @Test
    public void makeNewReservation(@Autowired InventoryService inventoryService) {
        // make reservation
        String key = "newSessionId";
        inventoryService.makeReservation(key, id1, 1);

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
        inventoryService.makeReservation(key, id1, 0);

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
        inventoryService.makeReservation(key, id1, 1);

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
        inventoryService.makeReservation(key, id1, 0);

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
            inventoryService.makeReservation(existingSession1, null, 1);
        });
    }

    @Test
    public void throwIAESessionIDReservation(@Autowired InventoryService inventoryService) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.makeReservation(null, id1, 1);
        });
    }

    @Test
    public void throwIAENegativeUnitsReservation(@Autowired InventoryService inventoryService) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.makeReservation(existingSession1, id1, -1);
        });
    }

    @Test
    public void throwIAEUnitsReservation(@Autowired InventoryService inventoryService) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.makeReservation(existingSession1, id1, 1000);
        });
    }

    @Test
    public void throwNSEEReservation(@Autowired InventoryService inventoryService) {
        Assertions.assertThrows(NoSuchElementException.class, () -> {
            inventoryService.makeReservation(existingSession1, "wrongid", 1);
        });
    }

    @Test
    public void commitReservations(@Autowired InventoryService inventoryService) {
        // make reservation
        String key = existingSession1;
        inventoryService.commitReservations(key);

        // assert things
        assertEquals(2, productRepository.count());

        // assert item id1
        InventoryItem actual = productRepository.findById(id1).get();

        assertEquals(14, actual.getUnits());
        assertEquals(9, actual.getAvailableUnits());

        List<Reservation> actualReservation = actual.getReservations();

        assertEquals(2, actualReservation.size());
        assertReservationAbsence(actualReservation, key);

        // assert item id2
        actual = productRepository.findById(id2).get();

        assertEquals(196, actual.getUnits());
        assertEquals(196, actual.getAvailableUnits());

        actualReservation = actual.getReservations();

        assertEquals(0, actualReservation.size());
        assertReservationAbsence(actualReservation, key);
    }

    @Test
    public void deleteReservations(@Autowired InventoryService inventoryService) {
        // delete reservation
        String key = existingSession1;
        inventoryService.deleteReservations(key);

        // assert things
        assertEquals(2, productRepository.count());

        // assert item id1
        InventoryItem actual = productRepository.findById(id1).get();

        assertEquals(15, actual.getUnits());
        assertEquals(10, actual.getAvailableUnits());

        List<Reservation> actualReservation = actual.getReservations();

        assertEquals(2, actualReservation.size());
        assertReservationAbsence(actualReservation, key);

        // assert item id2
        actual = productRepository.findById(id2).get();

        assertEquals(200, actual.getUnits());
        assertEquals(200, actual.getAvailableUnits());

        actualReservation = actual.getReservations();

        assertEquals(0, actualReservation.size());
        assertReservationAbsence(actualReservation, key);
    }
}
