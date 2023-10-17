package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartContent;
import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.order.web.SagaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
public class OrderService {

    private final CartService cartService;

    private final InventoryService inventoryService;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public OrderService(@Autowired CartService cartService, @Autowired InventoryService inventoryService) {
        this.cartService = cartService;
        this.inventoryService = inventoryService;
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
     * @throws OrderNotPlacedException if the order to confirm is empty/ would result in a negative sum
     */
    public void confirmOrder(String sessionId, String cardNumber, String cardOwner, String checksum)
        throws OrderNotPlacedException {
        // is it more reasonable to get total from cart service, or is it more
        // reasonable to pass the total from the front end (where it was displayed and
        // therefore is known) ??
        double total = getTotal(sessionId);

        if (total <= 0) {
            throw new OrderNotPlacedException(String
                .format("No Order placed for session %s. Cart is either empty or not available. ", sessionId));
        }

        SagaRequest request = new SagaRequest(sessionId, cardNumber, cardOwner, checksum, total);

        try {
//            ResponseEntity<Void> response = Retry
//                .decorateSupplier(retry, () -> template.postForEntity(orchestratorUrl, request, Void.class)).get();
            // TODO orchestrator.post(request)
            ResponseEntity<Void> response = null;

//            LOG.info("orchestrator accepted request for session {} with status code {}.", sessionId,
//                response.getStatusCode());

            cartService.deleteCart(sessionId);
            LOG.info("deleted cart for session {}.", sessionId);

        } catch (RestClientException e) {
            LOG.error("Failed to contact orchestrator for session {}. Exception: {}", sessionId, e);
            throw new OrderNotPlacedException(
                String.format("No Order placed for session %s. Orchestrator not available. ", sessionId));
        }
    }

    /**
     * Calculates the total of a users cart.
     * <p>
     * Depends on the cart service to get the cart content and depends on the inventory service to get the price per
     * unit. If either of them fails, the returned total is 0. This is because the store cannot handle partial orders.
     * Its either ordering all items in the cart or none.
     *
     * @param sessionId identifies the session to get total for
     * @return the total money to pay for products in the cart
     */
    private double getTotal(String sessionId) {
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
