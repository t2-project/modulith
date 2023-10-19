package de.unistuttgart.t2.modulith.inventory.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;

public final class InventoryProductMapper {
    public static Product toProduct(InventoryItem inventoryItem) {
        return new Product(
            inventoryItem.getId(),
            inventoryItem.getName(),
            inventoryItem.getDescription(),
            inventoryItem.getAvailableUnits(), // available units = units in stock - sum of reserved units
            inventoryItem.getPrice());
    }
    public static Optional<Product> toProduct(Optional<InventoryItem> optionalInventoryItem) {
        return optionalInventoryItem.map(InventoryProductMapper::toProduct);
    }

    public static InventoryItem toInventoryItem(Product product) {
        return new InventoryItem(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getUnits(),
            product.getPrice());
    }

    public static Optional<InventoryItem> toInventoryItem(Optional<Product> optionalProduct) {
        return optionalProduct.map(InventoryProductMapper::toInventoryItem);
    }
}
