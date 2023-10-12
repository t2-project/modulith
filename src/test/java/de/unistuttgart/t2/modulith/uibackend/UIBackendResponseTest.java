package de.unistuttgart.t2.modulith.uibackend;

import de.unistuttgart.t2.modulith.cart.CartModule;
import de.unistuttgart.t2.modulith.common.Product;
import de.unistuttgart.t2.modulith.uibackend.supplicants.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static de.unistuttgart.t2.modulith.uibackend.supplicants.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test whether UIBackendservice handles all responses correctly.
 * <p>
 * The Set up is like this:
 * <ul>
 * <li>Call the operation under test.
 * <li>Mock the responses that the operation would receive from other services.
 * <li>Assert that the operation under test processes the replies as intended.
 * </ul>
 *
 * @author maumau
 */
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class UIBackendResponseTest {

    @Mock
    CartModule cartModule;

    @InjectMocks
    private UIBackendService service;

    @Test
    public void getProductsInCartTest() {
        when(cartModule.getCart(sessionId)).thenReturn(cartResponse());

        // TODO Test inventory
//        ResponseEntity<String> inventoryEntity = new ResponseEntity<>(JSONs.inventoryResponse(), HttpStatus.OK);
//        Mockito.when(template.getForEntity(JSONs.inventoryUrl + JSONs.productId, String.class))
//            .thenReturn(inventoryEntity);

        // execute
        List<Product> products = service.getProductsInCart(TestData.sessionId);

        // assert
        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals(productId, products.get(0).getId());
        assertEquals(units, products.get(0).getUnits());
    }

    @Test
    public void getSingleProductTest() {
        // setup inventory responses
        // TODO Test inventory
//        ResponseEntity<String> entity = new ResponseEntity<>(inventoryResponse(), HttpStatus.OK);
//        Mockito.when(template.getForEntity(JSONs.inventoryUrl + JSONs.productId, String.class)).thenReturn(entity);

        // execute
        Product product = service.getSingleProduct(TestData.productId).get();

        // assert
        assertNotNull(product);
        assertEquals(TestData.productId, product.getId());
        assertEquals("name", product.getName());
        assertEquals("description", product.getDescription());
        assertEquals(5, product.getUnits());
        assertEquals(1.0, product.getPrice());
    }

    @Test
    public void getAllProductsTest() {
        // setup inventory responses
        // TODO Test inventory
//        ResponseEntity<String> entity = new ResponseEntity<>(inventoryResponseAllProducts(), HttpStatus.OK);
//        Mockito.when(template.getForEntity(JSONs.inventoryUrl, String.class)) // no id, we want ALL.
//            .thenReturn(entity);

        // execute
        List<Product> products = service.getAllProducts();

        // assert
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertEquals(productId, products.get(0).getId());
        assertEquals(anotherproductId, products.get(1).getId());
    }
}
