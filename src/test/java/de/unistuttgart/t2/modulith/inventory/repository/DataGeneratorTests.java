package de.unistuttgart.t2.modulith.inventory.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class DataGeneratorTests {

    @Autowired
    InventoryRepository productRepository;

    DataGenerator generator;

    @BeforeEach
    public void setup() {
        generator = new DataGenerator(productRepository, 10);
        productRepository.deleteAll();
    }

    @AfterEach
    public void cleanRepo() {
        productRepository.deleteAll();
    }

    @Test
    public void defaultDataGeneration() {
        assertEquals(0, productRepository.count());

        generator.generateProducts();

        assertEquals(10, productRepository.count());
    }

    @Test
    public void additionalDataGeneration() {
        assertEquals(0, productRepository.count());
        generator.generateProducts();
        assertEquals(10, productRepository.count());

        generator = new DataGenerator(productRepository, 15);
        generator.generateProducts();

        assertEquals(15, productRepository.count());
    }

    @Test
    public void noAdditionalDataGenerationIfThereAreAlreadyEnough() {
        assertEquals(0, productRepository.count());
        generator.generateProducts();
        assertEquals(10, productRepository.count());

        generator = new DataGenerator(productRepository, 5);
        generator.generateProducts();

        assertEquals(10, productRepository.count());
    }

    @Test
    public void restockProducts() {
        assertEquals(0, productRepository.count());
        generator.generateProducts();
        generator.restockProducts();

        List<InventoryItem> items = productRepository.findAll();

        assertFalse(items.isEmpty());

        for (InventoryItem item : items) {
            assertEquals(Integer.MAX_VALUE, item.getUnits());
        }
    }
}
