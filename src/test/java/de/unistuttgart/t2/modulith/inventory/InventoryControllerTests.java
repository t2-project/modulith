package de.unistuttgart.t2.modulith.inventory;

import de.unistuttgart.t2.modulith.inventory.repository.DataGenerator;
import de.unistuttgart.t2.modulith.inventory.web.InventoryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static de.unistuttgart.t2.modulith.TestData.inventoryResponseAllProducts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test the logic of {@link InventoryController}.
 *
 * @author maumau
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class InventoryControllerTests {

    @Mock
    InventoryService inventoryService;

    @Mock
    DataGenerator dataGenerator;

    InventoryController controller;

    @BeforeEach
    public void setUp() {
        controller = new InventoryController(inventoryService, dataGenerator);
    }

    @Test
    public void getAllProducts() {
        when(inventoryService.getAllProducts()).thenReturn(inventoryResponseAllProducts());

        List<Product> actual = controller.getAllProducts();

        assertEquals(2, actual.size());
    }
}
