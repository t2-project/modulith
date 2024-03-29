package de.unistuttgart.t2.modulith.inventory;

import de.unistuttgart.t2.modulith.inventory.repository.InventoryItem;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryProductMapper;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryRepository;
import de.unistuttgart.t2.modulith.inventory.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Manages the inventory and the reservations.
 * <p>
 * Reservations are distinguished by their session ids.
 *
 * @author maumau
 * @author davidkopp
 */
@Service
@Transactional
@EnableJpaRepositories(basePackageClasses = {InventoryRepository.class, ReservationRepository.class})
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(@Autowired InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Get a list of all products from the inventory.
     *
     * @return a list of all products in the inventory.
     */
    public List<Product> getAllProducts() {
        List<InventoryItem> inventoryItems = inventoryRepository.findAll();
        return inventoryItems.stream().map(InventoryProductMapper::toProduct).toList();
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
        Optional<InventoryItem> inventoryItem = inventoryRepository.findById(productId);
        return InventoryProductMapper.toProduct(inventoryItem);
    }

    /**
     * Get the products with the given productIds from the inventory.
     * <p>
     * If there are either no products with the given sessionId, or the retrieval of the products failed, an empty collection
     * is returned.
     *
     * @param productIds collection of product ids to be retrieved
     * @return products if product ids exists
     */
    public List<Product> getProducts(Collection<String> productIds) {
        List<InventoryItem> inventoryItems = inventoryRepository.findAllById(productIds);
        return inventoryItems.stream().map(InventoryProductMapper::toProduct).toList();
    }

    /**
     * commit reservations associated with given sessionId.
     *
     * @param sessionId to identify the reservations to delete
     */
    public void commitReservations(String sessionId) {
        List<InventoryItem> items = inventoryRepository.findAll();
        for (InventoryItem item : items) {
            item.commitReservation(sessionId);
        }
        inventoryRepository.saveAll(items);
    }

    /**
     * delete reservations of cancelled order from repository.
     *
     * @param sessionId to identify which reservations to delete
     */
    public void deleteReservations(String sessionId) {
        List<InventoryItem> items = inventoryRepository.findAll();
        for (InventoryItem item : items) {
            item.deleteReservation(sessionId);
        }
        inventoryRepository.saveAll(items);
    }

    /**
     * attach a reservation for the given session to the given item.
     *
     * @param sessionId user to reserve for
     * @param productId products to reserve of
     * @param units     amount to reserve
     * @return the item where the reservation was attached
     * @throws NoSuchElementException   if the product does not exist
     * @throws IllegalArgumentException if any parameter is null
     * @throws InsufficientUnitsAvailableException if not enough units are available
     */
    public Product makeReservation(String sessionId, String productId, int units) throws NoSuchElementException, InsufficientUnitsAvailableException {
        if (productId == null || sessionId == null || units < 0) {
            throw new IllegalArgumentException(
                "productId : " + productId + ", sessionId : " + sessionId + ", units : " + units);
        }
        InventoryItem item = inventoryRepository.findById(productId).orElseThrow(
            () -> new NoSuchElementException(String.format("product with id %s not found", productId)));

        item.addReservation(sessionId, units);
        InventoryItem savedItem = inventoryRepository.save(item);
        return InventoryProductMapper.toProduct(savedItem);
    }
}
