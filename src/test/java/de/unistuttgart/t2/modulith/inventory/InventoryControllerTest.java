package de.unistuttgart.t2.modulith.inventory;

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
 * Test the logic in the {@link InventoryController}.
 *
 * @author maumau
 */
@ExtendWith(MockitoExtension.class)
//@SpringJUnitConfig(TestContext.class)
@ActiveProfiles("test")
public class InventoryControllerTest {

    @Mock
    InventoryService inventoryService;

    InventoryController controller;

    @BeforeEach
    public void setUp() {
        controller = new InventoryController(inventoryService);
    }

    @Test
    public void test() {
        when(inventoryService.getAllProducts()).thenReturn(inventoryResponseAllProducts());

        List<Product> actual = controller.getAllProducts();

        assertEquals(2, actual.size());
    }
}
