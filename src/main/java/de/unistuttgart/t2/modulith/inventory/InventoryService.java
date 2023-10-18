package de.unistuttgart.t2.modulith.inventory;

import de.unistuttgart.t2.modulith.inventory.repository.InventoryItem;
import de.unistuttgart.t2.modulith.inventory.repository.ProductRepository;
import de.unistuttgart.t2.modulith.inventory.repository.Reservation;
import de.unistuttgart.t2.modulith.inventory.repository.ReservationRepository;
import de.unistuttgart.t2.modulith.inventory.web.ReservationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class InventoryService {

    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public InventoryService(@Autowired ProductRepository productRepository,
                            @Autowired ReservationRepository reservationRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
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
     * commit reservations associated with given sessionId.
     *
     * @param sessionId to identify the reservations to delete
     */
    public void handleSagaAction(String sessionId) {
        List<InventoryItem> items = productRepository.findAll();
        for (InventoryItem item : items) {
            item.commitReservation(sessionId);
        }
        productRepository.saveAll(items);

        List<Reservation> reservations = reservationRepository.findAll();
        for (Reservation reservation : reservations) {
            if (reservation.getUserId().equals(sessionId)) {
                reservationRepository.delete(reservation);
            }
        }
    }

    /**
     * delete reservations of cancelled order from repository.
     *
     * @param sessionId to identify which reservations to delete
     */
    public void handleSagaCompensation(String sessionId) {
        List<InventoryItem> items = productRepository.findAll();
        for (InventoryItem item : items) {
            item.deleteReservation(sessionId);

        }
        productRepository.saveAll(items);

        List<Reservation> reservations = reservationRepository.findAll();
        for (Reservation reservation : reservations) {
            if (reservation.getUserId().equals(sessionId)) {
                reservationRepository.delete(reservation);
            }
        }
    }

    /**
     * attach a reservation for the given session to the given item.
     *
     * @param productId products to reserve of
     * @param sessionId user to reserve for
     * @param units     amount to reserve
     * @return the item where the reservation was attached
     * @throws NoSuchElementException   if the product does not exist
     * @throws IllegalArgumentException if any parameter is null
     */
    public Product makeReservation(String productId, String sessionId, int units) throws NoSuchElementException {
        if (productId == null || sessionId == null || units < 0) {
            throw new IllegalArgumentException(
                "productId : " + productId + ", sessionId : " + sessionId + ", units : " + units);
        }
        InventoryItem item = productRepository.findById(productId).orElseThrow(
            () -> new NoSuchElementException(String.format("product with id %s not found", productId)));

        item.addReservation(sessionId, units);
        InventoryItem savedItem = productRepository.save(item);
        return new Product(savedItem.getId(), savedItem.getName(), savedItem.getDescription(), savedItem.getAvailableUnits(),
            savedItem.getPrice());
    }
}
