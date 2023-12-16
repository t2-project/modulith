package de.unistuttgart.t2.modulith.order.calculation;

import de.unistuttgart.t2.modulith.cart.CartContent;
import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Calculator to simulate a compute intensive task.
 * Calculates the total sum of order.
 *
 * @author davidkopp
 */
public class ComputeIntensiveTotalCalculator implements ITotalCalculator {

    private final CartService cartService;
    private final InventoryService inventoryService;
    private final int iterations;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public ComputeIntensiveTotalCalculator(CartService cartService, InventoryService inventoryService, int iterations) {
        this.cartService = cartService;
        this.inventoryService = inventoryService;
        this.iterations = iterations;
    }

    /**
     * Calculates the total of a users cart many times to simulate a compute intensive task.
     * <p>
     * Depends on the cart module to get the cart content and depends on the inventory module to get the price per
     * unit.
     *
     * @param sessionId identifies the session to get total for
     * @return the total money to pay for products in the cart
     */
    public double calculate(String sessionId) {
        LOG.debug("Compute intensive order calculation started with {} iterations", iterations);
        CartContent cart = cartService.getCart(sessionId).orElse(new CartContent());

        double total = 0;
        for (String productId : cart.getProductIds()) {
            Optional<Product> product = inventoryService.getSingleProduct(productId);
            if (product.isEmpty()) {
                return 0;
            }

            // simulate compute intensive task
            double temp = 0;
            for (int i = 0; i < iterations; i++) {
                temp += product.get().getPrice() * cart.getUnits(productId);
            }
            total += temp / iterations;
        }
        LOG.debug("Compute intensive order calculation finished");
        return total;
    }
}
