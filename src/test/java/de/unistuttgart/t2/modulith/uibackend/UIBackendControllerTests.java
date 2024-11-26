package de.unistuttgart.t2.modulith.uibackend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.t2.modulith.cart.CartContent;
import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.uibackend.exceptions.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.uibackend.exceptions.ReservationFailedException;
import de.unistuttgart.t2.modulith.uibackend.web.OrderRequest;
import de.unistuttgart.t2.modulith.uibackend.web.UIBackendController;
import de.unistuttgart.t2.modulith.uibackend.web.UpdateCartRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test the logic of {@link UIBackendController}.
 *
 * @author maumau
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UIBackendControllerTests {

    @Mock
    UIBackendService service;

    @Captor
    ArgumentCaptor<String> sessionIdCaptor;

    @Captor
    ArgumentCaptor<String> productIdCaptor;

    @Captor
    ArgumentCaptor<Integer> unitsCaptor;

    UIBackendController controller;

    @BeforeEach
    public void setUp() {
        controller = new UIBackendController(service);
    }

    @Test
    public void getAllProducts() {
        when(service.getAllProducts()).thenReturn(inventoryResponseAllProducts());

        List<Product> products = controller.getAllProducts();

        verify(service, times(1)).getAllProducts();
        assertEquals(2, products.size());
    }

    @Test
    public void getProduct() {
        when(service.getProduct(productId)).thenReturn(inventoryResponse());

        Product product = controller.getProduct(productId);

        verify(service, times(1)).getProduct(productId);
        assertEquals(productId, product.getId());
    }

    @Test
    public void getProductNotFoundThrowsException() {
        when(service.getProduct(productId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.getProduct(productId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void dontChangeCartIfUnitsAreZero() throws ReservationFailedException {

        CartContent cartContent = new CartContent(Map.of(productId, 0));

        UpdateCartRequest request = new UpdateCartRequest(cartContent.getContent());
        List<Product> addedProducts = controller.updateCart(sessionId, request);

        verify(service, never()).addItemToCart(anyString(), anyString(), anyInt());
        assertEquals(0, addedProducts.size());
    }

    @Test
    public void addItemToCart() throws ReservationFailedException {

        UpdateCartRequest request = new UpdateCartRequest(Map.of(productId, units));
        controller.updateCart(sessionId, request);

        verify(service, times(1)).addItemToCart(sessionIdCaptor.capture(), productIdCaptor.capture(),
                unitsCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(productId, productIdCaptor.getValue());
        assertEquals(units, unitsCaptor.getValue());
    }

    @Test
    public void addMultipleItemsToCart() throws ReservationFailedException {

        UpdateCartRequest request = new UpdateCartRequest(Map.of(productId, units, anotherProductId, anotherUnits));
        controller.updateCart(sessionId, request);

        verify(service, times(2)).addItemToCart(anyString(), productIdCaptor.capture(), unitsCaptor.capture());
        assertEquals(2, productIdCaptor.getAllValues().size());
        assertEquals(2, unitsCaptor.getAllValues().size());
    }

    @Test
    public void removeItemFromCart() throws ReservationFailedException {

        UpdateCartRequest request = new UpdateCartRequest(Map.of(productId, -units));
        List<Product> addedProducts = controller.updateCart(sessionId, request);

        verify(service, times(1)).deleteItemFromCart(sessionIdCaptor.capture(), productIdCaptor.capture(),
                unitsCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(productId, productIdCaptor.getValue());
        assertEquals(units, unitsCaptor.getValue());
        assertEquals(0, addedProducts.size(), "Expected that no product was added");
    }

    @Test
    public void confirmOrder() throws OrderNotPlacedException {

        OrderRequest request = new OrderRequest("cardNumber", "cardOwner", "checksum", sessionId);
        controller.confirmOrder(request);

        verify(service).confirmOrder(sessionIdCaptor.capture(), anyString(), anyString(), anyString());
        assertEquals(sessionId, sessionIdCaptor.getValue());
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
