package de.unistuttgart.t2.modulith.cart.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that the collector for expired carts works as expected.
 *
 * @author maumau
 */
@SpringBootTest
@ActiveProfiles("test")
class CollectorTests {

    @Autowired
    TimeoutCollector collector;

    @Autowired
    CartRepository repository;

    @BeforeEach
    public void populateRepository() {
        repository.deleteAll();
        for (int i = 0; i < 5; i++) {
            repository.save(new CartItem());
        }
    }

    @Test
    public void collectAllEntriesTest() throws InterruptedException {
        collector.cleanup();
        assertEquals(0, repository.count());
    }

    @Test
    public void collectSomeEntriesTest() throws InterruptedException {
        CartItem item = new CartItem();
        item.setCreationDate(Date.from(Instant.now().plusSeconds(60)));
        repository.save(item);

        collector.cleanup();
        assertEquals(1, repository.count());
    }
}
