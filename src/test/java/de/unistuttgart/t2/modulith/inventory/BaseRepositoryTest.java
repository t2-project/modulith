package de.unistuttgart.t2.modulith.inventory;

import de.unistuttgart.t2.modulith.inventory.repository.InventoryItem;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryRepository;
import de.unistuttgart.t2.modulith.inventory.repository.Reservation;
import de.unistuttgart.t2.modulith.inventory.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parent class to all tests that use the repositories. Populates the repositories and offers some helpers for assertion.
 *
 * @author maumau
 */
public abstract class BaseRepositoryTest {

    @Autowired
    InventoryRepository productRepository;

    @Autowired
    ReservationRepository reservationRepository;

    String id1, id2;

    String existingSession1 = "session1";
    String existingSession2 = "session2";
    String existingSession3 = "session3";

    /**
     * Populate the repositories.
     */
    @BeforeEach
    void populateRepository() {
        productRepository.deleteAll();
        reservationRepository.deleteAll();

        InventoryItem item1 = new InventoryItem("id1", "name1", "description1", 15, 0.5);

        item1.addReservation(existingSession1, 1);
        item1.addReservation(existingSession2, 2);
        item1.addReservation(existingSession3, 3);

        InventoryItem item2 = new InventoryItem("id2", "name2", "description2", 200, 1.5);
        item2.addReservation(existingSession1, 4);

        id1 = productRepository.save(item1).getId();
        id2 = productRepository.save(item2).getId();
    }

    /**
     * get the reservation for the user identified by key.
     *
     * @param actual the reservations, presumably as found in the db
     * @param key    the reservation to look for
     * @return reservation for user identified by key.
     */
    public Reservation getReservation(List<Reservation> actual, String key) {
        actual = actual.stream().filter(r -> r.getUserId().equals(key)).collect(Collectors.toList());
        assertEquals(1, actual.size());
        return actual.get(0);
    }

    /**
     * asserts that there is no reservation for user identified by key within actual.
     *
     * @param actual the reservations, presumably as found in the db
     * @param key    the reservation to look for
     */
    public void assertReservationAbsence(List<Reservation> actual, String key) {
        actual = actual.stream().filter(r -> r.getUserId().equals(key)).collect(Collectors.toList());
        assertTrue(actual.isEmpty());
    }
}
