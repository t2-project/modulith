package de.unistuttgart.t2.modulith.uibackend;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.t2.modulith.common.CartContent;
import de.unistuttgart.t2.modulith.common.Product;
import de.unistuttgart.t2.modulith.common.ReservationRequest;
import de.unistuttgart.t2.modulith.common.SagaRequest;
import de.unistuttgart.t2.modulith.uibackend.exceptions.CartInteractionFailedException;
import de.unistuttgart.t2.modulith.uibackend.exceptions.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.uibackend.exceptions.ReservationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages interaction with other services.
 *
 * @author maumau
 */
public class UIBackendService {

    @Autowired
    RestTemplate template;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);

    public UIBackendService() {
    }

    /**
     * Get a list of all products from the inventory.
     * <p>
     * TODO : the generated endpoints do things with pages. this gets the first twenty items only.
     *
     * @return a list of all products in the inventory. (might be incomplete)
     */
    public List<Product> getAllProducts() {
        List<Product> result = new ArrayList<>();

        LOG.debug("get from inventory");

        // first page
        // TODO result = inventory.getAll();
        // result.addAll(getSomeProducts());

        // additional pages
//            ResponseEntity<String> response = Retry
//                .decorateSupplier(retry, () -> template.getForEntity(inventoryUrl, String.class)).get();
//
//            JsonNode root = mapper.readTree(response.getBody());
//
//            while (hasNext(root)) {
//                String url = getNext(root);
//                result.addAll(getSomeProducts(url));
//
//                root = mapper.readTree(
//                    Retry.decorateSupplier(retry, () -> template.getForEntity(url, String.class)).get().getBody());
//
//            }
        return result;
    }

    /**
     * Add the given number units of product to a users cart.
     * <p>
     * If the product is already in the cart, the units of that product will be updated.
     *
     * @param sessionId identifies the cart to add to
     * @param productId id of product to be added
     * @param units     number of units to be added
     * @throws CartInteractionFailedException if the request number of unit could not be placed in the cart.
     */
    public void addItemToCart(String sessionId, String productId, int units) throws CartInteractionFailedException {
//      String ressourceUrl = cartUrl + sessionId;
        LOG.debug("put to cart: " + sessionId);

        Optional<CartContent> optCartContent = getCartContent(sessionId);

        if (optCartContent.isPresent()) {
            CartContent cartContent = optCartContent.get();
            cartContent.getContent().put(productId, units + cartContent.getUnits(productId));
//                template.put(ressourceUrl, cartContent);
            // TODO cart.put(cartContent)
        } else {
//                template.put(ressourceUrl, new CartContent(Map.of(productId, units)));
            // TODO cart.put(new CartContent(Map.of(productId, units)));
        }
    }

    /**
     * Delete the given number units of product from a users cart.
     * <p>
     * If the number of units in the cart decrease to zero or less, the product is remove from the cart. If the no such
     * product is in cart, do nothing.
     *
     * @param sessionId identifies the cart to delete from
     * @param productId id of the product to be deleted
     * @param units     number of units to be deleted
     * @throws CartInteractionFailedException if anything went wrong while talking to the cart
     */
    public void deleteItemFromCart(String sessionId, String productId, int units)
            throws CartInteractionFailedException {
//        String ressourceUrl = cartUrl + sessionId;
        LOG.debug("delete to cart: " + sessionId);

        Optional<CartContent> optCartContent = getCartContent(sessionId);

        if (optCartContent.isPresent()) {
//            try {
            CartContent cartContent = optCartContent.get();
            int remainingUnitsInCart = cartContent.getUnits(productId) + units;
            if (remainingUnitsInCart > 0) {
                cartContent.getContent().put(productId, remainingUnitsInCart);
            } else {
                cartContent.getContent().remove(productId);
            }
//                Retry.decorateRunnable(retry, () -> template.put(ressourceUrl, cartContent)).run();
            // TODO cart.put(cartContent);
//            } catch (RestClientException e) {
//                LOG.error("Cannot delete {} unit(s) of {} for {}. Exception: {}", units, productId, sessionId, e);
//                throw new CartInteractionFailedException(
//                    String.format("Deletion for session %s failed : %s, %d", sessionId, productId, units));
//            }
        }
    }

    /**
     * Delete the entire cart for the given sessionId.
     *
     * @param sessionId identifies the cart content to delete
     * @throws CartInteractionFailedException if anything went wrong while talking to the cart
     */
    public void deleteCart(String sessionId) throws CartInteractionFailedException {
//        String ressourceUrl = cartUrl + sessionId;
        LOG.debug("delete to cart: " + sessionId);

        try {
//            template.delete(ressourceUrl);
            // TODO cart.delete(sessionId);
        } catch (RestClientException e) {
            LOG.error("Cannot delete cart.", e);
        }
    }

    /**
     * Get a list of all products in a users cart.
     *
     * @param sessionId identfies the cart content to get
     * @return a list of the product in the cart
     */
    public List<Product> getProductsInCart(String sessionId) {
        List<Product> results = new ArrayList<>();

        Optional<CartContent> optCartContent = getCartContent(sessionId);

        if (optCartContent.isPresent()) {
            CartContent cartContent = optCartContent.get();

            for (String productId : cartContent.getProductIds()) {
                getSingleProduct(productId).ifPresent(p -> {
                    p.setUnits(cartContent.getUnits(productId));
                    results.add(p);
                });
            }
        }

        return results;
    }

    /**
     * Reserve the given number of units of the given product.
     *
     * @param sessionId identifies the session to reserve for
     * @param productId identifies the product to reserve of
     * @param units     number of units to reserve
     * @return the product the reservation was made for
     * @throws ReservationFailedException if the reservation could not be placed
     */
    public Product makeReservations(String sessionId, String productId, int units)
            throws ReservationFailedException {

//        String ressourceUrl = inventoryUrl + reservationEndpoint;
        LOG.debug("post to inventory: " + sessionId);
        try {
            ReservationRequest request = new ReservationRequest(productId, sessionId, units);

//            ResponseEntity<Product> inventoryResponse = Retry
//                .decorateSupplier(retry, () -> template.postForEntity(ressourceUrl, request, Product.class)).get();
            // TODO inventory.post(request)
            return null;
//            return inventoryResponse.getBody();
        } catch (RestClientException e) {
            LOG.error("Cannot reserve {} units of {} for {}. Exception: {}", units, productId, sessionId, e);
            throw new ReservationFailedException(
                    String.format("Reservation for session %s failed : %s, %d", sessionId, productId, units));
        }
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

            LOG.info("orchestrator accepted request for session {} with status code {}.", sessionId,
                    response.getStatusCode());

            deleteCart(sessionId);
            LOG.info("deleted cart for session {}.", sessionId);

        } catch (RestClientException e) {
            LOG.error("Failed to contact orchestrator for session {}. Exception: {}", sessionId, e);
            throw new OrderNotPlacedException(
                    String.format("No Order placed for session %s. Orchestrator not available. ", sessionId));
        } catch (CartInteractionFailedException e) {
            LOG.error("Failed to delete cart for session {}. Exception: {}", sessionId);
        }
    }

    /**
     * Get the content of the cart belonging to the given sessionId.
     * <p>
     * If there is either no cart content for the given sessionId, or the retrieval of the content failed, an empty
     * optional is returned.
     *
     * @param sessionId the session id of the client whose cart content to retrieve
     * @return content of cart iff it exists
     */
    protected Optional<CartContent> getCartContent(String sessionId) {
//        String ressourceUrl = cartUrl + sessionId;
        LOG.debug("get from cart: " + sessionId);
        // TODO cart.getContent(sessionId)
//        try {
//            ResponseEntity<String> response = Retry
//                .decorateFunction(retry, (String url) -> template.getForEntity(url, String.class))
//                .apply(ressourceUrl);
//
//            JsonNode root = mapper.readTree(response.getBody());
//            JsonNode name = root.path("content");
//
//            return Optional.of(mapper.treeToValue(name, CartContent.class));
//        } catch (RestClientException e) { // 404 or something like that.
//            LOG.error("Cannot find any cart content for {}. Exception: {} ", sessionId, e);
//        } catch (JsonProcessingException e) { // whatever we received, it was no cart content.
//            LOG.error("Cannot deserialize cart content. Exception: {}", e);
//        }
        return Optional.empty();
    }

    /**
     * Get the product with the given productId from the inventory.
     * <p>
     * If there is either no product with the given sessionId, or the retrieval of the product failed, an empty optional
     * is returned.
     *
     * @param productId id of the product to be retrieved
     * @return product with given id iff it exists
     */
    protected Optional<Product> getSingleProduct(String productId) {
//        String ressourceUrl = inventoryUrl + productId;
        LOG.debug("get from inventory: " + productId);

        try {
//            ResponseEntity<String> response = Retry
//                .decorateFunction(retry, (String url) -> template.getForEntity(url, String.class))
//                .apply(ressourceUrl);
            // TODO inventory.get(productId);
            Product product = null;

            // important, because inventory api may (did) return more fields than we need.
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//            Product product = mapper.readValue(response.getBody(), Product.class);
            product.setId(productId);

            return Optional.of(product);
        } catch (RestClientException e) {
            LOG.error("Cannot get product {}. Exception: {}", productId, e);
        }
        return Optional.empty();
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
        CartContent cart = getCartContent(sessionId).orElse(new CartContent());

        double total = 0;

        for (String productId : cart.getProductIds()) {
            Optional<Product> product = getSingleProduct(productId);
            if (product.isEmpty()) {
                return 0;
            }
            total += product.get().getPrice() * cart.getUnits(productId);
        }
        return total;
    }
}
