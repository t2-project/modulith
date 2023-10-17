package de.unistuttgart.t2.modulith.inventory.web;

import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("products")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(@Autowired InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * @return a list of all products in the inventory
     */
    @Operation(summary = "List all available products")
    @GetMapping
    public List<Product> getAllProducts() {

        return inventoryService.getAllProducts();
    }
}
