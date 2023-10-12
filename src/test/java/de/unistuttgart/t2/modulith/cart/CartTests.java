package de.unistuttgart.t2.modulith.cart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import de.unistuttgart.t2.modulith.common.CartContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the generated endpoints.
 * <p>
 * Set up like this:
 * <ul>
 * <li>Make request to cart endpoints.
 * <li>Assert correctness of reply
 * </ul>
 * This is kind of stupid because the endpoints are generated and I'd assume that generated stuff works as intended.
 *
 * @author maumau
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestContext.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CartTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private int initialSize;

    @BeforeEach
    public void populateRepository(@Autowired CartRepository repository) {
        CartItem emptyCart = new CartItem("foo");
        CartItem filledCart = new CartItem("bar", Map.of("id1", 3, "id2", 4));
        repository.save(emptyCart);
        repository.save(filledCart);

        initialSize = repository.findAll().size();
    }

    @Test
    public void getEmptyCartTest(@Autowired CartRepository repository)
        throws JsonMappingException, JsonProcessingException {
        // make request
        String response = restTemplate.getForObject("http://localhost:" + port + "/cart/foo", String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        // assert deserialization
        CartContent cc = mapper.treeToValue(root.path("content"), CartContent.class);
        assertNotNull(cc);
        assertNotNull(cc.getContent());
        assertTrue(cc.getContent().isEmpty());
    }

    @Test
    public void getFullCartTest(@Autowired CartRepository repository)
        throws JsonMappingException, JsonProcessingException {

        // make request
        String response = restTemplate.getForObject("http://localhost:" + port + "/cart/bar", String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        // assert deserialization
        CartContent cc = mapper.treeToValue(root.path("content"), CartContent.class);
        assertNotNull(cc);
        assertNotNull(cc.getContent());
        assertFalse(cc.getContent().isEmpty());

        // asser content
        Map<String, Integer> expected = repository.findById("bar").get().getContent();
        Map<String, Integer> actual = cc.getContent();

        for (String key : expected.keySet()) {
            assertTrue(actual.containsKey(key), "missing key " + key);
            assertEquals(expected.get(key), actual.get(key), "wrong value for key " + key);
        }
    }

    @Test
    public void putNewCartTest(@Autowired CartRepository repository) {
        // make request
        String id = "baz";
        String key = "id3";
        int value = 15;
        CartContent cc = new CartContent(Map.of(key, value));
        restTemplate.put("http://localhost:" + port + "/cart/" + id, cc);

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
    public void putUpdateCartTest(@Autowired CartRepository repository) {
        // make request
        String id = "bar";
        String key = "id3";
        int value = 15;
        CartContent cc = new CartContent(Map.of(key, value));
        restTemplate.put("http://localhost:" + port + "/cart/" + id, cc);

        assertTrue(repository.existsById(id));
        assertEquals(initialSize, repository.findAll().size());

        CartItem item = repository.findById(id).get();

        assertNotNull(item.getContent());
        assertFalse(item.getContent().isEmpty());

        assertTrue(item.getContent().containsKey(key));
        assertEquals(value, item.getContent().get(key));
    }

    @Test
    public void deleteCartTest(@Autowired CartRepository repository) {
        // make request
        String id = "bar";
        restTemplate.delete("http://localhost:" + port + "/cart/" + id);

        assertFalse(repository.existsById(id));
        assertEquals(initialSize - 1, repository.findAll().size());
    }
}
