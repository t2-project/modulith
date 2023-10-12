package de.unistuttgart.t2.modulith.cart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import de.unistuttgart.t2.modulith.common.CartContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the logic of the cart module using MongoDB embedded database.
 *
 * @author maumau
 */
@SpringBootTest
@ActiveProfiles("test")
class CartTests {

    private int initialSize;

    private CartModule cartModule;

    private CartRepository repository;

    @BeforeEach
    public void populateRepository(@Autowired CartModule cartModule, @Autowired CartRepository repository) {
        this.cartModule = cartModule;
        this.repository = repository;

        CartItem emptyCart = new CartItem("foo");
        CartItem filledCart = new CartItem("bar", Map.of("id1", 3, "id2", 4));
        repository.save(emptyCart);
        repository.save(filledCart);

        initialSize = repository.findAll().size();

//        cartModule = new CartModule();
    }

    @Test
    public void getEmptyCartTest() {
        // make request
        Optional<CartContent> optionalCartContent = cartModule.getCart("foo");

        // assert
        assertTrue(optionalCartContent.isPresent());
        CartContent cc = optionalCartContent.get();
        assertNotNull(cc);
        assertNotNull(cc.getContent());
        assertTrue(cc.getContent().isEmpty());
    }

    @Test
    public void getFullCartTest()
        throws JsonMappingException, JsonProcessingException {

        // make request
        Optional<CartContent> optionalCartContent = cartModule.getCart("bar");

        // assert object
        assertTrue(optionalCartContent.isPresent());
        CartContent cc = optionalCartContent.get();
        assertNotNull(cc);
        assertNotNull(cc.getContent());
        assertFalse(cc.getContent().isEmpty());

        // assert content
        Map<String, Integer> expected = repository.findById("bar").get().getContent();
        Map<String, Integer> actual = cc.getContent();

        for (String key : expected.keySet()) {
            assertTrue(actual.containsKey(key), "missing key " + key);
            assertEquals(expected.get(key), actual.get(key), "wrong value for key " + key);
        }
    }

    @Test
    public void putNewCartTest() {
        // make request
        String id = "baz";
        String key = "id3";
        int value = 15;
        CartContent cc = new CartContent(Map.of(key, value));

        cartModule.saveCart(id, cc);

        // asser repository
        assertTrue(repository.existsById(id));
        assertEquals(initialSize + 1, repository.findAll().size());

        CartItem item = repository.findById(id).get();

        assertNotNull(item.getContent());
        assertFalse(item.getContent().isEmpty());

        assertTrue(item.getContent().containsKey(key));
        assertEquals(value, item.getContent().get(key));
    }

    @Test
    public void putUpdateCartTest() {
        // make request
        String id = "bar";
        String key = "id3";
        int value = 15;
        CartContent cc = new CartContent(Map.of(key, value));

        cartModule.saveCart(id, cc);

        assertTrue(repository.existsById(id));
        assertEquals(initialSize, repository.findAll().size());

        CartItem item = repository.findById(id).get();

        assertNotNull(item.getContent());
        assertFalse(item.getContent().isEmpty());

        assertTrue(item.getContent().containsKey(key));
        assertEquals(value, item.getContent().get(key));
    }

    @Test
    public void deleteCartTest() {
        // make request
        String id = "bar";

        cartModule.deleteCart(id);

        assertFalse(repository.existsById(id));
        assertEquals(initialSize - 1, repository.findAll().size());
    }
}
