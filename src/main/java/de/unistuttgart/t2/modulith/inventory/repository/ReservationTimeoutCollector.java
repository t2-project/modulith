package de.unistuttgart.t2.modulith.inventory.repository;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Periodically checks all reservations and deletes those whose time to life has been exceeded.<br>
 *
 * @author maumau
 */
@Component
public class ReservationTimeoutCollector {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    /** in seconds */
    private final long TTL;
    /** in milliseconds */
    private final int taskRate;

    private final ReservationRepository repository;
    private final InventoryRepository itemRepository;
    private final ThreadPoolTaskScheduler taskScheduler;

    /**
     * Create collector.
     *
     * @param TTL            the cart entries' time to live in seconds
     * @param taskRate       rate at which the collector checks the repo in milliseconds
     * @param taskScheduler  the scheduler to use for this collector
     * @param repository     the repository that handles the reservation data
     * @param itemRepository the repository that handles the item data
     */
    public ReservationTimeoutCollector(@Value("${t2.inventory.TTL:0}") final long TTL,
                                       @Value("${t2.inventory.taskRate:0}") final int taskRate,
                                       @Autowired final ThreadPoolTaskScheduler taskScheduler,
                                       @Autowired final ReservationRepository repository,
                                       @Autowired final InventoryRepository itemRepository) {
        assert TTL >= 0 && taskRate >= 0 && taskScheduler != null && repository != null && itemRepository != null;
        this.TTL = TTL;
        this.taskRate = taskRate;
        this.taskScheduler = taskScheduler;
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    /**
     * Schedule the task to check reservations and delete them if necessary.
     * <p>
     * If either the TTL or the taskRate is 0, no task will be scheduled.
     */
    @PostConstruct
    public void schedulePeriodically() {
        if (taskRate > 0) {
            taskScheduler.scheduleAtFixedRate(this::cleanup, Duration.ofMillis(taskRate));
        }
    }

    /**
     * The actual cleanup.
     */
    public void cleanup() {
        List<Reservation> items = repository.findAll();
        Date latestDateAlive = Date.from(Instant.now().minusSeconds(TTL));

        Collection<Reservation> expiredReservations =
            items.stream().filter(r -> r.getCreationDate().before(latestDateAlive)).collect(Collectors.toSet());

        LOG.info("Found {} expired reservations", expiredReservations.size());

        deleteAtItems(expiredReservations);
        repository.deleteAll(expiredReservations);
    }

    public void deleteAtItems(Collection<Reservation> rs) {
        for (Reservation r : rs) {

            InventoryItem i = itemRepository.findById(r.item.getId()).get();
            i.deleteReservation(r.getUserId());
            itemRepository.save(i);
        }
    }
}
