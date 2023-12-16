package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.order.calculation.ComputeIntensiveTotalCalculator;
import de.unistuttgart.t2.modulith.order.calculation.SimpleTotalCalculator;
import de.unistuttgart.t2.modulith.order.calculation.ITotalCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CalculatorTests {

    @Mock
    CartService cartService;

    @Mock
    InventoryService inventoryService;

    @BeforeEach
    public void beforeEach() {
        when(cartService.getCart(sessionId)).thenReturn(cartResponseMulti());
        when(inventoryService.getSingleProduct(productId)).thenReturn(inventoryResponse());
        when(inventoryService.getSingleProduct(anotherProductId)).thenReturn(anotherInventoryResponse());
    }

    @Test
    public void defaultCalculator() {
        ITotalCalculator calculator = new SimpleTotalCalculator(cartService, inventoryService);
        double result = calculator.calculate(sessionId);

        assertEquals(totalOfCartMulti, result);
    }

    @Test
    public void computeIntensiveCalculator() {
        int iterations = 1_000_0000; // use a higher number like e.g. 1_000_000_000 to see that it is actually compute intensive
        ITotalCalculator calculator = new ComputeIntensiveTotalCalculator(cartService, inventoryService, iterations);
        double result = calculator.calculate(sessionId);

        assertEquals(totalOfCartMulti, result);
    }
}
