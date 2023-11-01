package de.unistuttgart.t2.modulith.cart.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that the collector for expired carts works as expected.
 *
 * @author maumau
 * @author davidkopp
 */
@DataMongoTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class CartCollectorTests {

    CartTimeoutCollector collector;

    @Autowired
    CartRepository repository;

    @BeforeEach
    public void populateRepository() {
        // Automatic task scheduling is not tested, therefore it is set to null.
        this.collector = new CartTimeoutCollector(repository, null, 0, 0);

        repository.deleteAll();
        for (int i = 0; i < 5; i++) {
            repository.save(new CartItem());
        }
    }

    @Test
    public void cleanupCollectsAllEntries() {
        collector.cleanup();
        assertEquals(0, repository.count());
    }

    @Test
    public void cleanupCollectsOnlyOldEntries() {
        CartItem item = new CartItem();
        item.setCreationDate(Date.from(Instant.now().plusSeconds(60)));
        repository.save(item);

        collector.cleanup();
        assertEquals(1, repository.count());
    }
}
