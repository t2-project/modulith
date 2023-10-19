package de.unistuttgart.t2.modulith.cart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.t2.modulith.cart.web.CartController;
import de.unistuttgart.t2.modulith.cart.web.UpdateCartRequest;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test the logic of {@link CartController}.
 *
 * @author maumau
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CartControllerTests {

    @Mock
    CartService cartService;

    @Mock
    InventoryService inventoryService;

    @Captor
    ArgumentCaptor<String> sessionIdCaptor;

    @Captor
    ArgumentCaptor<String> productIdCaptor;

    @Captor
    ArgumentCaptor<Integer> unitsCaptor;

    CartController controller;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        controller = new CartController(cartService, inventoryService);
    }

    @Test
    public void dontChangeCartIfUnitsAreZero() {

        CartContent cartContent = new CartContent(Map.of(productId, 0));

        UpdateCartRequest request = new UpdateCartRequest(cartContent.getContent());
        List<Product> addedProducts = controller.updateCart(sessionId, request);

        verify(cartService, never()).addItemToCart(anyString(), anyString(), anyInt());
        assertEquals(0, addedProducts.size());
    }

    @Test
    public void addItemToCart() throws JsonProcessingException {

        Product product = productBase(productId, units);
        when(inventoryService.makeReservation(productId, sessionId, units)).thenReturn(product);

        UpdateCartRequest request = new UpdateCartRequest(Map.of(productId, units));
        System.out.println(mapper.writeValueAsString(request));
        List<Product> addedProducts = controller.updateCart(sessionId, request);

        verify(cartService, times(1)).addItemToCart(sessionIdCaptor.capture(), productIdCaptor.capture(), unitsCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(productId, productIdCaptor.getValue());
        assertEquals(units, unitsCaptor.getValue());
        assertEquals(1, addedProducts.size());
        assertEquals(product, addedProducts.get(0));
    }

    @Test
    public void addMultipleItemsToCart() {

        Product product1 = productBase(productId, units);
        Product product2 = productBase(anotherProductId, anotherUnits);
        when(inventoryService.makeReservation(productId, sessionId, units)).thenReturn(product1);
        when(inventoryService.makeReservation(anotherProductId, sessionId, anotherUnits)).thenReturn(product2);

        UpdateCartRequest request = new UpdateCartRequest(Map.of(productId, units, anotherProductId, anotherUnits));
        List<Product> addedProducts = controller.updateCart(sessionId, request);

        verify(cartService, times(2)).addItemToCart(anyString(), productIdCaptor.capture(), unitsCaptor.capture());
        assertEquals(2, productIdCaptor.getAllValues().size());
        assertEquals(2, unitsCaptor.getAllValues().size());
        assertEquals(2, addedProducts.size());
    }

    @Test
    public void removeItemFromCart() {

        UpdateCartRequest request = new UpdateCartRequest(Map.of(productId, -units));
        List<Product> addedProducts = controller.updateCart(sessionId, request);

        verify(cartService, times(1)).deleteItemFromCart(sessionIdCaptor.capture(), productIdCaptor.capture(), unitsCaptor.capture());
        verify(inventoryService, never()).makeReservation(anyString(), anyString(), anyInt());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(productId, productIdCaptor.getValue());
        assertEquals(units, unitsCaptor.getValue());
        assertEquals(0, addedProducts.size(), "Expected that no product was added");
    }

    @Test
    public final void updateCartRequestSerializingAndDeserializing() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        UpdateCartRequest original = new UpdateCartRequest(
            Map.of("c1e359ff-4cd7-4ede-93fb-378aced160e5", 1));
        String serialized = mapper.writeValueAsString(original);
        UpdateCartRequest deserialized = mapper.reader()
            .forType(UpdateCartRequest.class)
            .readValue(serialized);

        assertEquals(original.getContent(), deserialized.getContent());
    }
}
