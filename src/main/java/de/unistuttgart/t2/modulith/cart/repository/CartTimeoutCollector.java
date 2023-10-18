package de.unistuttgart.t2.modulith.cart.repository;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Periodically checks all reservations and deletes those whose time to life has been exceeded.
 * <p>
 * (apparently there is a mongo native attach on expiry date to documents, but i didn't find anything on whether this
 * also works with the spring repository interface. thus the manual deletion.)
 *
 * @author maumau
 */
@Component
public class CartTimeoutCollector {

    private static final Logger LOG = LoggerFactory.getLogger(CartTimeoutCollector.class);

    private final long TTL; // seconds
    private final int taskRate; // milliseconds

    @Autowired
    private CartRepository repository;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    /**
     * Create collector.
     *
     * @param TTL      the cart entries' time to live in seconds
     * @param taskRate rate at which the collector checks the repo in milliseconds
     */
    @Autowired
    public CartTimeoutCollector(@Value("${t2.cart.TTL:0}") long TTL, @Value("${t2.cart.taskRate:0}") int taskRate) {
        this.TTL = TTL;
        this.taskRate = taskRate;
    }

    /**
     * Schedule the task to check cart contents and delete them if necessary.
     * <p>
     * If the taskRate is 0, no task will be scheduled.
     */
    @PostConstruct
    public void scheduleTask() {
        if (taskRate > 0) {
            taskScheduler.scheduleAtFixedRate(this::cleanup, Duration.ofMillis(taskRate));
        }
    }

    void cleanup() {
        Collection<String> expiredCarts = getExpiredCarts();
        deleteItems(expiredCarts);
        LOG.info(String.format("deleted %d expired carts", expiredCarts.size()));
    }

    /**
     * Get all ids of expired carts.
     * <p>
     * The get step is separated from the delete step because i want to lock the db as little as possible and need not
     * do it for getting the ids.
     * <p>
     * Carts that were created earlier than {@code TTL} seconds before 'now' are expired.
     *
     * @return all expired cart IDs
     */
    private Collection<String> getExpiredCarts() {
        return repository.findAll().parallelStream()
            .filter(item -> item.getCreationDate().before(Date.from(Instant.now().minusSeconds(TTL))))
            .map(CartItem::getId).collect(Collectors.toList());
    }

    /**
     * Delete cart from repository.
     *
     * @param ids the ids of all carts to delete
     */
    @Transactional
    public void deleteItems(Collection<String> ids) {
        repository.deleteByIdIn(ids);
    }
}
