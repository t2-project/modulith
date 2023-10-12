package de.unistuttgart.t2.modulith.uibackend;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.unistuttgart.t2.modulith.common.CartContent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Provides test data for the tests.
 * 
 * @author maumau
 */
public class TestData {

    public static String productId = "foo";
    public static int units = 42;
    public static String anotherproductId = "foo2";
    public static int anotherunits = 42;
    public static String sessionId = "bar";

    public static Optional<CartContent> cartResponse() {
        return Optional.of(new CartContent(new HashMap<>(Map.of(productId, units))));
    }

    public static Optional<CartContent> cartResponseMulti() {
        return Optional.of(new CartContent(new HashMap<>(Map.of(productId, units, anotherproductId, anotherunits))));
    }

    public static Optional<CartContent> updatedCartResponse() {
        return Optional.of(new CartContent(new HashMap<>(Map.of(productId, 43))));
    }

    public static String inventoryResponse() {
//        ObjectNode base = inventoryBase("name", "description");
//        JsonNode links = makeLinks(inventoryUrl + productId, "inventory");
//
//        base.set("_links", links);
//
//        return base.toString();

        // TODO inventory response
        return "";
    }

    public static ObjectNode inventoryBase(String name, String description) {
//        ObjectNode base = factory.objectNode();
//        base.set("name", factory.textNode(name));
//        base.set("description", factory.textNode(description));
//        base.set("units", factory.numberNode(5));
//        base.set("price", factory.numberNode(1.0));
//
//        return base;

        // TODO inventory base
        return null;
    }

    public static String anotherInventoryResponse() {
//        ObjectNode base = inventoryBase("name2", "description2");
//        JsonNode links = makeLinks(inventoryUrl + anotherproductId, "inventory");
//
//        base.set("_links", links);
//
//        return base.toString();

        // TODO inventory response (another product)
        return "";
    }

    public static String inventoryResponseAllProducts() {
//        JsonNode links1 = makeLinks(inventoryUrl + productId, "inventory");
//        JsonNode links2 = makeLinks(inventoryUrl + anotherproductId, "inventory");
//        ObjectNode base1 = inventoryBase("name", "description");
//        ObjectNode base2 = inventoryBase("name2", "description2");
//
//        base1.set("_links", links1);
//        base2.set("_links", links2);
//
//        JsonNode inventroy = factory.arrayNode().add(base1).add(base2);
//        ObjectNode all = factory.objectNode().set("inventory", inventroy);
//        ObjectNode embedded = factory.objectNode().set("_embedded", all);
//
//        return embedded.toString();

        // TODO inventory response all products
        return "";
    }
}
