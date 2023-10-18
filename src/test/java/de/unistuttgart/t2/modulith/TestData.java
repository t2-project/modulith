package de.unistuttgart.t2.modulith;

import de.unistuttgart.t2.modulith.cart.CartContent;
import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.inventory.Product;

import java.util.*;

/**
 * Provides test data for the tests.
 *
 * @author maumau
 */
public final class TestData {

    public static String productId = "foo";
    public static int units = 42;
    public static String anotherProductId = "foo2";
    public static int anotherUnits = 42;
    public static String sessionId = "bar";

    public static Optional<CartContent> cartResponse() {
        return Optional.of(new CartContent(new HashMap<>(Map.of(productId, units))));
    }

    public static Optional<CartItem> cartItemResponse() {
        return Optional.of(new CartItem(sessionId, new HashMap<>(Map.of(productId, units))));
    }

    public static Optional<CartContent> cartResponseMulti() {
        return Optional.of(new CartContent(new HashMap<>(Map.of(productId, units, anotherProductId, anotherUnits))));
    }

    public static Optional<CartContent> updatedCartResponse() {
        return Optional.of(new CartContent(new HashMap<>(Map.of(productId, 43))));
    }

    public static List<Product> productsBasedOnCartContent(CartContent cartContent) {
        List<Product> products = new ArrayList<>();
        for (String productId : cartContent.getContent().keySet()) {
            Integer units = cartContent.getContent().get(productId);
            products.add(productBase(productId, units));
        }
        return products;
    }

    public static Product productBase(String productId, int units) {
        return new Product(productId, "name", "description", units, 1.0);
    }

    public static Optional<Product> inventoryResponse() {
        return Optional.of(new Product(productId, "name", "description", 5, 1.0));
    }

    public static Optional<Product> anotherInventoryResponse() {
        return Optional.of(new Product(productId, "name2", "description2", 5, 1.0));
    }

    public static List<Product> inventoryResponseAllProducts() {
        Product product1 = inventoryResponse().get();
        Product product2 = anotherInventoryResponse().get();
        return new ArrayList<>(List.of(product1, product2));
    }
}
