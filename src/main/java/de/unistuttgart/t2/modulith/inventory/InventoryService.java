package de.unistuttgart.t2.modulith.inventory;

import de.unistuttgart.t2.modulith.inventory.web.ReservationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    /**
     * Get a list of all products from the inventory.
     * <p>
     * TODO : the generated endpoints do things with pages. this gets the first twenty items only.
     *
     * @return a list of all products in the inventory. (might be incomplete)
     */
    public List<Product> getAllProducts() {
        List<Product> result = new ArrayList<>();

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
     * Get the product with the given productId from the inventory.
     * <p>
     * If there is either no product with the given sessionId, or the retrieval of the product failed, an empty optional
     * is returned.
     *
     * @param productId id of the product to be retrieved
     * @return product with given id if it exists
     */
    public Optional<Product> getSingleProduct(String productId) {
//        String ressourceUrl = inventoryUrl + productId;
//        LOG.debug("get from inventory: " + productId);

        try {
//            ResponseEntity<String> response = Retry
//                .decorateFunction(retry, (String url) -> template.getForEntity(url, String.class))
//                .apply(ressourceUrl);
            // TODO inventoryRepository.get(productId);
            Product product = null;

            // important, because inventory api may (did) return more fields than we need.
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//            Product product = mapper.readValue(response.getBody(), Product.class);
            product.setId(productId);

            return Optional.of(product);
        } catch (RestClientException e) {
//            LOG.error("Cannot get product {}. Exception: {}", productId, e);
        }
        return Optional.empty();
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
}
