package de.unistuttgart.t2.modulith.inventory;

import de.unistuttgart.t2.modulith.TestData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

// TODO Enable inventory service tests
@Disabled
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class InventoryServiceTests {

    @InjectMocks
    InventoryService inventoryService;

    @Test
    public void getAllProducts() {
        // setup inventory responses
        // TODO Test inventory
//        ResponseEntity<String> entity = new ResponseEntity<>(inventoryResponseAllProducts(), HttpStatus.OK);
//        Mockito.when(template.getForEntity(JSONs.inventoryUrl, String.class)) // no id, we want ALL.
//            .thenReturn(entity);

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
        // setup inventory responses
        // TODO Test inventory
//        ResponseEntity<String> entity = new ResponseEntity<>(inventoryResponse(), HttpStatus.OK);
//        Mockito.when(template.getForEntity(JSONs.inventoryUrl + JSONs.productId, String.class)).thenReturn(entity);

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
    public void makeReservation() throws ReservationFailedException {
//        ReservationRequest request = new ReservationRequest(productId, sessionId, 2);

//        mockServer.expect(ExpectedCount.once(), requestTo(reservationUrl)).andExpect(method(HttpMethod.POST))
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(content().json(mapper.writeValueAsString(request)))
//            .andRespond(withSuccess(inventoryResponse(), MediaType.APPLICATION_JSON));
        // TODO Test when reservation then

        // execute
        Product reservedProduct = inventoryService.makeReservation(sessionId, productId, 2);

        assertEquals(productId, reservedProduct.getId());
        assertEquals(2, reservedProduct.getUnits());
    }
}
