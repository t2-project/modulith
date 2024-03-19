package de.unistuttgart.t2.modulith.inventory;

import de.unistuttgart.t2.modulith.TestData;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryItem;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryProductMapper;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryRepository;
import de.unistuttgart.t2.modulith.inventory.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class InventoryServiceTests {

    @Mock
    InventoryRepository productRepository;

    @Mock
    ReservationRepository reservationRepository;

    InventoryService inventoryService;

    @BeforeEach
    public void setup() {
        inventoryService = new InventoryService(productRepository, reservationRepository);
    }

    @Test
    public void getAllProducts() {
        // setup inventory response
        List<Product> allProducts = inventoryResponseAllProducts();
        List<InventoryItem> inventoryItems = allProducts.stream().map(InventoryProductMapper::toInventoryItem).toList();
        when(productRepository.findAll()).thenReturn(inventoryItems);

        // execute
        List<Product> products = inventoryService.getAllProducts();

        // assert
        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals(productId, products.get(0).getId());
        assertEquals(anotherProductId, products.get(1).getId());
    }

    @Test
    public void getSingleProduct() {
        // setup inventory response
        Optional<Product> productInInventory = inventoryResponse();
        Optional<InventoryItem> inventoryItem = productInInventory.map(InventoryProductMapper::toInventoryItem);
        when(productRepository.findById(productId)).thenReturn(inventoryItem);

        // execute
        Product product = inventoryService.getSingleProduct(TestData.productId).get();

        // assert
        assertNotNull(product);
        assertEquals(TestData.productId, product.getId());
        assertEquals("name", product.getName());
        assertEquals("description", product.getDescription());
        assertEquals(5, product.getUnits());
        assertEquals(1.0, product.getPrice());
    }

    @Test
    public void getProducts() {
        // setup inventory response
        List<Product> allProducts = inventoryResponseAllProducts();
        List<InventoryItem> inventoryItems = allProducts.stream().map(InventoryProductMapper::toInventoryItem).toList();
        List<String> ids = inventoryItems.stream().map(InventoryItem::getId).toList();
        when(productRepository.findAllById(ids)).thenReturn(inventoryItems);

        // execute
        List<Product> products = inventoryService.getProducts(ids);

        // assert
        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals(productId, products.get(0).getId());
        assertEquals(anotherProductId, products.get(1).getId());
    }

    @Test
    public void makeReservation() throws InsufficientUnitsAvailableException {
        // setup inventory response
        Optional<Product> productInInventory = inventoryResponse();
        Optional<InventoryItem> inventoryItem = productInInventory.map(InventoryProductMapper::toInventoryItem);
        when(productRepository.findById(productId)).thenReturn(inventoryItem);

        InventoryItem inventoryItemWithReservation = productInInventory.map(InventoryProductMapper::toInventoryItem).get();
        inventoryItemWithReservation.addReservation(sessionId, 2);
        when(productRepository.save(any())).thenReturn(inventoryItemWithReservation);

        // execute
        Product reservedProduct = inventoryService.makeReservation(sessionId, productId, 2);

        // assert
        assertEquals(productId, reservedProduct.getId());
        int expectedAvailableUnits = productInInventory.get().getUnits() - 2;
        assertEquals(expectedAvailableUnits, reservedProduct.getUnits());
    }
}
