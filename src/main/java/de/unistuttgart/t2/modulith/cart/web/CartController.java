package de.unistuttgart.t2.modulith.cart.web;

import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.inventory.ReservationFailedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides the http endpoints of the cart module.
 *
 * @author davidkopp
 */
@RestController
@RequestMapping("cart")
public class CartController {
    private final CartService cartService;

    private InventoryService inventoryService;

    public CartController(@Autowired CartService cartService, @Autowired InventoryService inventoryService) {
        this.cartService = cartService;
        this.inventoryService = inventoryService;
    }

    /**
     * Get a list of all products in the user's cart.
     *
     * @param sessionId the session id of the user
     * @return a list of all products in the users cart
     */
    @Operation(summary = "List all items in cart")
    @GetMapping("/cart/{sessionId}")
    public List<Product> getCart(@PathVariable String sessionId) {
        return cartService.getProductsInCart(sessionId);
    }

    /**
     * Update units of the given products to the cart.
     * <p>
     * Add something to the cart, if the number of units is positive or delete from the cart when it is negative. Only
     * add the products to the cart if the requested number of unit is available. To achieve this, at first a
     * reservations are placed in the inventory and only after the reservations are succeeded be are the products added
     * to the cart.
     *
     * @param sessionId         sessionId to identify the user's cart
     * @param updateCartRequest request that contains the id of the products to be updated and the number of units to be
     *                          added or deleted
     * @return list of successfully added items
     */
    @Operation(summary = "Update items in cart")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = "{\n\"content\": {\n    \"product-id\": 3\n  }\n}")))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Cart updated")})
    @PostMapping(value = "/{sessionId}", produces = "application/json")
    public List<Product> updateCart(@PathVariable String sessionId, @RequestBody UpdateCartRequest updateCartRequest) {
        List<Product> successfullyAddedProducts = new ArrayList<>();

        for (Map.Entry<String, Integer> product : updateCartRequest.getContent().entrySet()) {
            if (product.getValue() == 0) {
                continue;
            }
            if (product.getValue() > 0) {
                try {
                    // contact inventory first, cause i'd rather have a dangling reservation than a
                    // products in the cart that are not backed with reservations.
                    Product addedProduct = inventoryService.makeReservations(sessionId, product.getKey(), product.getValue());
                    cartService.addItemToCart(sessionId, product.getKey(), product.getValue());
                    successfullyAddedProducts.add(addedProduct);
                } catch (ReservationFailedException e) {
                }
            } else { // product.getValue() < 0
                cartService.deleteItemFromCart(sessionId, product.getKey(), Math.abs(product.getValue()));
            }
        }
        return successfullyAddedProducts;
    }
}
