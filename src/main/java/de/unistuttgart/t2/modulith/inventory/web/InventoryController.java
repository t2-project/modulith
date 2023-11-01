package de.unistuttgart.t2.modulith.inventory.web;

import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.inventory.repository.DataGenerator;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Defines additional endpoints for the inventory. Other endpoints are auto generated.
 *
 * @author maumau
 */
@RestController
public class InventoryController {

    private final InventoryService inventoryService;
    private final DataGenerator dataGenerator;

    public InventoryController(@Autowired InventoryService inventoryService, @Autowired DataGenerator dataGenerator) {
        this.inventoryService = inventoryService;
        this.dataGenerator = dataGenerator;
    }

    /**
     * @return a list of all products in the inventory
     */
    @Operation(summary = "List all available products")
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return inventoryService.getAllProducts();
    }

    /**
     * add a reservation to a product.
     *
     * @param body request body
     * @return the product that the reservation was added to.
     */
    @Operation(summary = "Place a reservation", description = "Place a reservation of a number of units for a certain item for a certain user.")
    @PostMapping("/inventory/reservation")
    public Product addReservation(@RequestBody ReservationRequest body) {
        return inventoryService.makeReservation(body.getSessionId(), body.getProductId(),
            body.getUnits());
    }

    /**
     * trigger generation of new products TODO post x generation request seems more reasonable
     */
    @Operation(summary = "Populate the store with new products")
    @GetMapping("/generate")
    public void generateData() {
        dataGenerator.generateProducts();
    }

    /**
     * trigger restock of all products TODO post x restock request seems more reasonable
     */
    @Operation(summary = "Restock units of the store's products")
    @GetMapping("/restock")
    public void restock() {
        dataGenerator.restockProducts();
    }
}
