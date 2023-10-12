package de.unistuttgart.t2.modulith.cart.repository;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableAutoConfiguration
@EnableMongoRepositories(basePackageClasses = { CartRepository.class })
@Profile("test")
public class TestContext {

    @Bean
    public TimeoutCollector timeoutCollector() {
        return new TimeoutCollector(0, 0);
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }
}
