package de.unistuttgart.t2.modulith.cart.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestContext.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
/**
 * Tests that the collector for expired carts works as expected.
 *
 * @author maumau
 */
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
