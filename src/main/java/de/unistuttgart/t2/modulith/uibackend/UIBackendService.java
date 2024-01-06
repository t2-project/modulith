package de.unistuttgart.t2.modulith.uibackend;

import de.unistuttgart.t2.modulith.cart.CartContent;
import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.computation_simulator.ComputationSimulatorService;
import de.unistuttgart.t2.modulith.inventory.InsufficientUnitsAvailableException;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.order.OrderService;
import de.unistuttgart.t2.modulith.uibackend.exceptions.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.uibackend.exceptions.ReservationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages interaction with other modules.
 *
 * @author maumau
 * @author davidkopp
 */
@Service
public class UIBackendService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final CartService cartService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final boolean enableComputeIntensiveSimulation;
    private ComputationSimulatorService computationSimulatorService;

    @Autowired
    public UIBackendService(CartService cartService,
                            InventoryService inventoryService,
                            OrderService orderService,
                            @Value("${t2.computation-simulator.enabled}") boolean enableComputeIntensiveSimulation) {
        this.cartService = cartService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.enableComputeIntensiveSimulation = enableComputeIntensiveSimulation;

        if (enableComputeIntensiveSimulation) {
            LOG.warn("Simulate compute intensive task enabled! It will be executed when an order gets confirmed.");
        }
    }

    public UIBackendService(CartService cartService, InventoryService inventoryService, OrderService orderService) {
        this.cartService = cartService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.enableComputeIntensiveSimulation = false;
    }

    @Lazy
    @Autowired
    public void setComputationSimulatorService(ComputationSimulatorService computationSimulatorService) {
        this.computationSimulatorService = computationSimulatorService;
    }

    /**
     * Get a list of all products from the inventory.
     *
     * @return a list of all products in the inventory.
     */
    public List<Product> getAllProducts() {
        return inventoryService.getAllProducts();
    }

    /**
     * Add the given number units of product to a users cart.
     * <p>
     * If the product is already in the cart, the units of that product will be updated.
     *
     * @param sessionId identifies the cart to add to
     * @param productId id of product to be added
     * @param units     number of units to be added (must not be negative)
     * @return successfully added item
     * @throws ReservationFailedException if there is an error making reservations for the product
     */
    public Product addItemToCart(String sessionId, String productId, int units) throws ReservationFailedException {
        // contact inventory first, cause i'd rather have a dangling reservation than a
        // products in the cart that are not backed with reservations.
        Product addedProduct = null;
        try {
            addedProduct = inventoryService.makeReservation(sessionId, productId, units);
            addedProduct.setUnits(units);
        } catch (InsufficientUnitsAvailableException e) {
            throw new ReservationFailedException(String.format(
                "Adding item %s with %s units to cart of session %s failed. Reason: %s",
                productId, units, sessionId, e.getMessage()));
        }
        cartService.addItemToCart(sessionId, productId, units);
        return addedProduct;
    }

    /**
     * Delete the given number units of product from a users cart.
     * <p>
     * If the number of units in the cart decrease to zero or less, the product is remove from the cart. If the no such
     * product is in cart, do nothing.
     *
     * @param sessionId identifies the cart to delete from
     * @param productId id of the product to be deleted
     * @param units     number of units to be deleted (must not be negative)
     */
    public void deleteItemFromCart(String sessionId, String productId, int units) {
        cartService.deleteItemFromCart(sessionId, productId, units);
    }

    /**
     * Delete the entire cart for the given sessionId.
     *
     * @param sessionId identifies the cart content to delete
     */
    public void deleteCart(String sessionId) {
        cartService.deleteCart(sessionId);
    }

    /**
     * Get a list of all products in a users cart.
     *
     * @param sessionId identifies the cart content to get
     * @return a list of the product in the cart
     */
    public List<Product> getProductsInCart(String sessionId) {
        List<Product> results = new ArrayList<>();

        Optional<CartContent> cart = cartService.getCart(sessionId);

        if (cart.isPresent()) {
            CartContent cartContent = cart.get();

            for (String productId : cartContent.getProductIds()) {
                inventoryService.getSingleProduct(productId).ifPresent(p -> {
                    p.setUnits(cartContent.getUnits(productId));
                    results.add(p);
                });
            }
        }

        return results;
    }

    /**
     * Posts a request to start a transaction to the orchestrator. Attempts to delete the cart of the given sessionId
     * once the orchestrator accepted the request. Nothing happens if the deletion of a cart fails, as the cart service
     * supposed to periodically remove out dated cart entries anyway.
     *
     * @param sessionId  identifies the session
     * @param cardNumber part of payment details
     * @param cardOwner  part of payment details
     * @param checksum   part of payment details
     */
    public void confirmOrder(String sessionId, String cardNumber, String cardOwner, String checksum) throws OrderNotPlacedException {
        try {
            orderService.confirmOrder(sessionId, cardNumber, cardOwner, checksum);
        } catch (Exception e) {
            throw new OrderNotPlacedException(e.getMessage());
        }

        if (enableComputeIntensiveSimulation) {
            simulateComputeIntensiveTask(sessionId);
        }
    }

    /**
     * Calls the computation simulator to simulate a compute intensive scenario.
     * This method is blocking and waits until the computation is finished!
     */
    private void simulateComputeIntensiveTask(String sessionId) {
        LOG.info("Start simulation of an intensive computation task ... Session: {}", sessionId);
        // Returns the duration in milliseconds that the calculation took
        Double duration = computationSimulatorService.doCompute();
        LOG.info("Finished simulation of an intensive computation task. Duration: {} ms, Session: {}", duration, sessionId);
    }
}
