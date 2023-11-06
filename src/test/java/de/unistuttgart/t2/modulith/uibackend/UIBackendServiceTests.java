package de.unistuttgart.t2.modulith.uibackend;

import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.inventory.exceptions.InsufficientUnitsAvailableException;
import de.unistuttgart.t2.modulith.order.OrderService;
import de.unistuttgart.t2.modulith.uibackend.exceptions.ReservationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test whether UIBackendService makes the right requests to other modules.
 * <p>
 * The setup is like this:
 * <ul>
 * <li>Call the operation under test.
 * <li>Uses the mock server to receive the request and verify that it is placed as intended.
 * </ul>
 *
 * @author maumau
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UIBackendServiceTests {

    @Mock
    private CartService cartService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private OrderService orderService;

    @Captor
    ArgumentCaptor<String> sessionIdCaptor;

    @Captor
    ArgumentCaptor<String> productIdCaptor;

    @Captor
    ArgumentCaptor<Integer> unitsCaptor;

    private UIBackendService service;

    @BeforeEach
    public void setUp() {
        service = new UIBackendService(cartService, inventoryService, orderService);
    }

    @Test
    public void confirmOrder() throws Exception {

        // execute
        service.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");

        // assert
        verify(orderService).confirmOrder(sessionIdCaptor.capture(), anyString(), anyString(), anyString());
        assertEquals(sessionId, sessionIdCaptor.getValue());
    }

    @Test
    public void getAllProducts() {

        // setup
        when(inventoryService.getAllProducts()).thenReturn(inventoryResponseAllProducts());

        // execute
        List<Product> result = service.getAllProducts();

        // assert
        assertEquals(2, result.size());
    }

    @Test
    public void addItemToCart() throws InsufficientUnitsAvailableException, ReservationFailedException {

        // setup
        Product product = inventoryResponse().get();
        when(inventoryService.makeReservation(sessionId, productId, units)).thenReturn(product);

        // execute
        Product result = service.addItemToCart(sessionId, productId, units);

        // assert
        verify(cartService).addItemToCart(sessionIdCaptor.capture(), productIdCaptor.capture(), unitsCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(productId, productIdCaptor.getValue());
        assertEquals(units, unitsCaptor.getValue());

        verify(inventoryService).makeReservation(sessionIdCaptor.capture(), productIdCaptor.capture(), unitsCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(productId, productIdCaptor.getValue());
        assertEquals(units, unitsCaptor.getValue());

        assertEquals(product, result);
    }

    @Test
    public void deleteItemFromCart() {

        // execute
        service.deleteItemFromCart(sessionId, productId, units);

        // assert
        verify(cartService).deleteItemFromCart(sessionIdCaptor.capture(), productIdCaptor.capture(), unitsCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(productId, productIdCaptor.getValue());
        assertEquals(units, unitsCaptor.getValue());
    }

    @Test
    public void getProductsInCart() {
        // setup
        when(cartService.getCart(sessionId)).thenReturn(cartResponse());
        when(inventoryService.getSingleProduct(productId)).thenReturn(inventoryResponse());

        // execute
        List<Product> result = service.getProductsInCart(sessionId);

        // assert
        assertEquals(1, result.size());
        assertEquals(productId, result.get(0).getId());
        assertEquals(units, result.get(0).getUnits());
    }
}
