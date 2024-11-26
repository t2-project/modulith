package de.unistuttgart.t2.modulith.uibackend.web;

import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.uibackend.UIBackendService;
import de.unistuttgart.t2.modulith.uibackend.exceptions.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.uibackend.exceptions.ReservationFailedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Defines the http endpoints of the UIBackend.
 * These endpoints are not used by the UI, but can be used e.g. for load testing
 * the application.
 *
 * @author maumau
 * @author davidkopp
 */
@RestController
public class UIBackendController {

    private final UIBackendService service;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public UIBackendController(@Autowired UIBackendService service) {
        this.service = service;
    }

    /**
     * Get all existing products in the inventory
     * 
     * @return list of products
     */
    @Operation(summary = "List all available products", description = "Retrieve a list of all available products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of products retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Product.class)))),
    })
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return service.getAllProducts();
    }

    /**
     * Get a specific product by its ID
     * 
     * @param productId ID of the product
     * @return product if ID exists
     */
    @Operation(summary = "Get product by ID", description = "Retrieve a product by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully", content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(example = "{\"message\": \"Product with ID '123' not found\"}")))
    })
    @GetMapping("/products/{productId}")
    public Product getProduct(@PathVariable String productId) {
        return service.getProduct(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product with ID '" + productId + "' not found"));
    }

    /**
     * Update units of the given products to the cart.
     * <p>
     * Add something to the cart, if the number of units is positive or delete from
     * the cart when it is negative. Only
     * add the products to the cart if the requested number of unit is available. To
     * achieve this, at first a
     * reservations are placed in the inventory and only after the reservations are
     * succeeded be are the products added
     * to the cart.
     *
     * @param sessionId         sessionId to identify the user's cart
     * @param updateCartRequest request that contains the id of the products to be
     *                          updated and the number of units to be
     *                          added or deleted
     * @return list of successfully added items
     */
    @Operation(summary = "Update items in cart")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = "{\n\"content\": {\n    \"product-id\": 3\n  }\n}")))
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Cart updated"),
            @ApiResponse(responseCode = "500", description = "Cart could not be updated") })
    @PostMapping("/cart/{sessionId}")
    public List<Product> updateCart(@PathVariable String sessionId, @RequestBody UpdateCartRequest updateCartRequest)
            throws ReservationFailedException {
        List<Product> successfullyAddedProducts = new ArrayList<>();

        for (Map.Entry<String, Integer> product : updateCartRequest.getContent().entrySet()) {
            if (product.getValue() == 0) {
                continue;
            }
            if (product.getValue() > 0) {
                Product addedProduct = service.addItemToCart(sessionId, product.getKey(), product.getValue());
                successfullyAddedProducts.add(addedProduct);
            } else { // product.getValue() < 0
                service.deleteItemFromCart(sessionId, product.getKey(), Math.abs(product.getValue()));
            }
        }
        return successfullyAddedProducts;
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
        return service.getProductsInCart(sessionId);
    }

    /**
     * Place an order, i.e. start a transaction.<br>
     * Upon successfully placing the order, the cart is cleared and the session gets
     * invalidated.<br>
     * If the user wants to place another order he needs a new http session.
     *
     * @param request request to place an Order
     * @throws OrderNotPlacedException if the order could not be placed.
     */
    @Operation(summary = "Order all items in the cart", description = "Order all items in the cart")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Order for items is placed"),
            @ApiResponse(responseCode = "500", description = "Order could not be placed") })
    @PostMapping("/confirm")
    public void confirmOrder(@RequestBody OrderRequest request)
            throws OrderNotPlacedException {
        service.confirmOrder(request.getSessionId(), request.getCardNumber(), request.getCardOwner(),
                request.getChecksum());
    }

    /**
     * Creates the response entity if a request could not be served because a custom
     * exception was thrown.
     *
     * @param exception the exception that was thrown
     * @return a response entity with an exceptional message
     */
    @ExceptionHandler({ OrderNotPlacedException.class, ReservationFailedException.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleCustomException(Exception exception) {
        LOG.error("Internal server error. Caused by: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }
}
