package de.unistuttgart.t2.modulith.cart;

import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the logic of the cart module using MongoDB embedded database.
 *
 * @author maumau
 */
@DataMongoTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class CartDBTests {

    private int initialSize;

    private CartService cartService;

    @Autowired
    private CartRepository repository;

    @BeforeEach
    public void populateRepository() {
        this.cartService = new CartService(repository);

        CartItem emptyCart = new CartItem("foo");
        CartItem filledCart = new CartItem("bar", Map.of("id1", 3, "id2", 4));
        repository.save(emptyCart);
        repository.save(filledCart);

        initialSize = repository.findAll().size();
    }

    @Test
    public void getEmptyCart() {
        Optional<CartContent> optionalCartContent = cartService.getCart("foo");

        assertTrue(optionalCartContent.isPresent());
        CartContent cc = optionalCartContent.get();
        assertNotNull(cc);
        assertNotNull(cc.getContent());
        assertTrue(cc.getContent().isEmpty());
    }

    @Test
    public void getFullCart() {
        Optional<CartContent> optionalCartContent = cartService.getCart("bar");

        assertTrue(optionalCartContent.isPresent());
        CartContent cc = optionalCartContent.get();
        assertNotNull(cc);
        assertNotNull(cc.getContent());
        assertFalse(cc.getContent().isEmpty());

        Map<String, Integer> expected = repository.findById("bar").get().getContent();
        Map<String, Integer> actual = cc.getContent();

        for (String key : expected.keySet()) {
            assertTrue(actual.containsKey(key), "missing key " + key);
            assertEquals(expected.get(key), actual.get(key), "wrong value for key " + key);
        }
    }

    @Test
    public void addNewCart() {
        String id = "baz";
        String key = "id3";
        int value = 15;

        cartService.addItemToCart(id, key, value);

        assertTrue(repository.existsById(id));
        assertEquals(initialSize + 1, repository.findAll().size());

        CartItem item = repository.findById(id).get();

        assertNotNull(item.getContent());
        assertFalse(item.getContent().isEmpty());

        assertTrue(item.getContent().containsKey(key));
        assertEquals(value, item.getContent().get(key));
    }

    @Test
    public void addItemsToExistingCart() {
        String id = "bar";
        String key = "id3";
        int value = 15;

        cartService.addItemToCart(id, key, value);

        assertTrue(repository.existsById(id));
        assertEquals(initialSize, repository.findAll().size());

        CartItem item = repository.findById(id).get();

        assertNotNull(item.getContent());
        assertFalse(item.getContent().isEmpty());

        assertTrue(item.getContent().containsKey(key));
        assertEquals(value, item.getContent().get(key));
    }

    @Test
    public void deleteOneItemFromExistingCart() {
        String id = "bar";
        String key = "id1";
        int value = 3;

        cartService.deleteItemFromCart(id, key, value);

        assertTrue(repository.existsById(id));
        assertEquals(initialSize, repository.findAll().size());

        CartItem item = repository.findById(id).get();

        assertNotNull(item.getContent());
        assertFalse(item.getContent().isEmpty());

        assertFalse(item.getContent().containsKey(key), "Expected that item was deleted");
    }

    @Test
    public void deleteSomeUnitsOfAnItemFromExistingCart() {
        String id = "bar";
        String key = "id1";
        int value = 2;

        cartService.deleteItemFromCart(id, key, value);

        assertTrue(repository.existsById(id));
        assertEquals(initialSize, repository.findAll().size());

        CartItem item = repository.findById(id).get();

        assertNotNull(item.getContent());
        assertFalse(item.getContent().isEmpty());

        assertTrue(item.getContent().containsKey(key));
        assertEquals(1, item.getContent().get(key));    }

    @Test
    public void deleteCart() {
        String id = "bar";

        cartService.deleteCart(id);

        assertFalse(repository.existsById(id));
        assertEquals(initialSize - 1, repository.findAll().size());
    }
}
