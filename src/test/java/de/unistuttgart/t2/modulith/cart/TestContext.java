package de.unistuttgart.t2.modulith.cart;

import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableAutoConfiguration
@EnableMongoRepositories(basePackageClasses = { CartRepository.class })
@Profile("test")
public class TestContext {}
