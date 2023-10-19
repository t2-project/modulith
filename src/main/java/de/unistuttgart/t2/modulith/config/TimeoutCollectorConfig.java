package de.unistuttgart.t2.modulith.config;

import de.unistuttgart.t2.modulith.cart.repository.CartTimeoutCollector;
import de.unistuttgart.t2.modulith.inventory.repository.ReservationTimeoutCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration of the thread pool used by
 * {@link CartTimeoutCollector CartTimeoutCollector} and
 * {@link ReservationTimeoutCollector ReserverationTimeoutCollector}.
 *
 * @author maumau
 */
@Configuration
public class TimeoutCollectorConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }
}
