package de.unistuttgart.t2.modulith.order.calculation;

import de.unistuttgart.t2.modulith.cart.CartContent;
import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;

import java.util.Optional;

/**
 * Simple calculator to get total sum of order (default).
 *
 * @author davidkopp
 */
public class SimpleTotalCalculator implements ITotalCalculator {

    private final CartService cartService;
    private final InventoryService inventoryService;

    public SimpleTotalCalculator(CartService cartService, InventoryService inventoryService) {
        this.cartService = cartService;
        this.inventoryService = inventoryService;
    }

    /**
     * Calculates the total of a users cart.
     * <p>
     * Depends on the cart module to get the cart content and depends on the inventory module to get the price per
     * unit.
     *
     * @param sessionId identifies the session to get total for
     * @return the total money to pay for products in the cart
     */
    public double calculate(String sessionId) {
        CartContent cart = cartService.getCart(sessionId).orElse(new CartContent());

        double total = 0;

        for (String productId : cart.getProductIds()) {
            Optional<Product> product = inventoryService.getSingleProduct(productId);
            if (product.isEmpty()) {
                return 0;
            }
            total += product.get().getPrice() * cart.getUnits(productId);
        }
        return total;
    }
}
