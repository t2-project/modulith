package de.unistuttgart.t2.modulith.uibackend;

import de.unistuttgart.t2.modulith.cart.CartModule;
import de.unistuttgart.t2.modulith.common.CartContent;
import de.unistuttgart.t2.modulith.common.Product;
import de.unistuttgart.t2.modulith.common.UpdateCartRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.unistuttgart.t2.modulith.uibackend.supplicants.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Test the logic in the {@link UIBackendController}.
 *
 * @author maumau
 */
@ExtendWith(MockitoExtension.class)
//@SpringJUnitConfig(TestContext.class)
@ActiveProfiles("test")
public class UIBackendControllerTest {

    @InjectMocks
    UIBackendService service;

    @Mock
    CartModule cartModule;

    UIBackendController controller;

    @Captor
    ArgumentCaptor<CartContent> cartContentCaptor;

    @BeforeEach
    public void setUp() {
        controller = new UIBackendController(service);
    }

    @Test
    public void test() {
//        mockServer.expect(ExpectedCount.twice(), requestTo(inventoryUrl)).andExpect(method(HttpMethod.GET))
//                .andRespond(withSuccess(inventoryResponseAllProducts(), MediaType.APPLICATION_JSON));
        // TODO Inventory test
//        when(inventoryModule.getInventory()).thenReturn(inventoryResponseAllProducts());

        List<Product> actual = controller.getAllProducts();

        assertEquals(2, actual.size());
    }

    @Test
    public void testDontChangeCart() {

        CartContent cartContent = new CartContent(Map.of(productId, 0));

        UpdateCartRequest request = new UpdateCartRequest(cartContent.getContent());
        controller.updateCart(sessionId, request);

        verify(cartModule, never()).saveCart(anyString(), any());
    }

    @Test
    public void testAddToCart() {

//        mockServer.expect(ExpectedCount.once(), requestTo(reservationUrl)).andExpect(method(HttpMethod.POST))
//                .andRespond(withSuccess(cartResponse(), MediaType.APPLICATION_JSON));
        // TODO Test with reservation
//
//        mockServer.expect(ExpectedCount.twice(), requestTo(cartUrl + sessionId)).andExpect(method(HttpMethod.GET))
//                .andRespond(withStatus(HttpStatus.NOT_FOUND));
//
//        mockServer.expect(ExpectedCount.once(), requestTo(cartUrl + sessionId)).andExpect(method(HttpMethod.PUT))
//                .andExpect(jsonPath("$.content." + productId).value(units))
//                .andRespond(withSuccess(cartResponse(), MediaType.APPLICATION_JSON));

        when(cartModule.getCart(sessionId)).thenReturn(Optional.empty());

        CartContent cartContent = new CartContent(Map.of(productId, units));
        UpdateCartRequest request = new UpdateCartRequest(cartContent.getContent());
        controller.updateCart(sessionId, request);

        verify(cartModule, times(1)).saveCart(anyString(), cartContentCaptor.capture());
        assertEquals(cartContent.getContent(), cartContentCaptor.getValue().getContent());
    }

    @Test
    public void testIncreaseCart() {

//        mockServer.expect(ExpectedCount.once(), requestTo(reservationUrl)).andExpect(method(HttpMethod.POST))
//                .andRespond(withSuccess(cartResponse(), MediaType.APPLICATION_JSON));
        // TODO Test with reservation
//
//        mockServer.expect(ExpectedCount.once(), requestTo(cartUrl + sessionId)).andExpect(method(HttpMethod.GET))
//                .andRespond(withSuccess(cartResponse(), MediaType.APPLICATION_JSON));
//
//        mockServer.expect(ExpectedCount.once(), requestTo(cartUrl + sessionId)).andExpect(method(HttpMethod.PUT))
//                .andExpect(jsonPath("$.content." + productId).value(units * 2))
//                .andRespond(withSuccess(cartResponse(), MediaType.APPLICATION_JSON));

        when(cartModule.getCart(sessionId)).thenReturn(cartResponse());

        UpdateCartRequest request = new UpdateCartRequest(Map.of(productId, units));
        controller.updateCart(sessionId, request);

        verify(cartModule, times(1)).saveCart(anyString(), cartContentCaptor.capture());
        assertEquals(units * 2, cartContentCaptor.getValue().getContent().get(productId));
    }

    @Test
    public void testDecreaseCart() {

//        mockServer.expect(ExpectedCount.once(), requestTo(cartUrl + sessionId)).andExpect(method(HttpMethod.GET))
//                .andRespond(withSuccess(cartResponse(), MediaType.APPLICATION_JSON));
//
//        mockServer.expect(ExpectedCount.once(), requestTo(cartUrl + sessionId)).andExpect(method(HttpMethod.PUT))
//                .andExpect(jsonPath("$.content." + productId).value(units - 1))
//                .andRespond(withSuccess(cartResponse(), MediaType.APPLICATION_JSON));

        when(cartModule.getCart(sessionId)).thenReturn(cartResponse());

        UpdateCartRequest request = new UpdateCartRequest(Map.of(productId, -1));
        controller.updateCart(sessionId, request);

        verify(cartModule, times(1)).saveCart(anyString(), cartContentCaptor.capture());
        assertEquals(units - 1, cartContentCaptor.getValue().getContent().get(productId));
    }

    @Test
    public void testRemoveFromCart() {

//        mockServer.expect(ExpectedCount.once(), requestTo(cartUrl + sessionId)).andExpect(method(HttpMethod.GET))
//                .andRespond(withSuccess(cartResponse(), MediaType.APPLICATION_JSON));
//
//        mockServer.expect(ExpectedCount.once(), requestTo(cartUrl + sessionId)).andExpect(method(HttpMethod.PUT))
//                .andExpect(jsonPath("$.content." + productId).doesNotExist())
//                .andRespond(withSuccess(cartResponse(), MediaType.APPLICATION_JSON));

        when(cartModule.getCart(sessionId)).thenReturn(cartResponse());

        UpdateCartRequest request = new UpdateCartRequest(Map.of(productId, -units));
        controller.updateCart(sessionId, request);

        CartContent expectedCartContent = new CartContent(); // empty
        verify(cartModule, times(1)).saveCart(anyString(), cartContentCaptor.capture());
        assertTrue(cartContentCaptor.getValue().getContent().isEmpty(), "Expected cart content is empty");
    }
}
