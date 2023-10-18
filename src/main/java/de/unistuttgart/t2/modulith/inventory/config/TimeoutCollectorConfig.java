package de.unistuttgart.t2.modulith.inventory.config;

import de.unistuttgart.t2.modulith.inventory.repository.ReservationTimeoutCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration to have all beans required for the {@link ReservationTimeoutCollector CartTimeoutCollector}.
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
