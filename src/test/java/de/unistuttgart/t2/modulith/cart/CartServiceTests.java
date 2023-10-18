package de.unistuttgart.t2.modulith.cart;

import de.unistuttgart.t2.modulith.TestData;
import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CartServiceTests {

    @InjectMocks
    CartService cartService;

    @Mock
    CartRepository cartRepository;

    @Mock
    InventoryService inventoryService;

    @Captor
    ArgumentCaptor<CartItem> cartItemCaptor;

    @Test
    public void addItemToCart() {
        when(cartRepository.findById(sessionId)).thenReturn(cartItemResponse());

        cartService.addItemToCart(sessionId, productId, 1);

        verify(cartRepository, times(1)).save(cartItemCaptor.capture());
        assertEquals(sessionId, cartItemCaptor.getValue().getId());
        assertEquals(units + 1, cartItemCaptor.getValue().getContent().get(productId));
    }

    @Test
    public void deleteItemFromCart() {
        when(cartRepository.findById(sessionId)).thenReturn(cartItemResponse());

        cartService.deleteItemFromCart(sessionId, productId, 1);

        verify(cartRepository, times(1)).save(cartItemCaptor.capture());
        assertEquals(sessionId, cartItemCaptor.getValue().getId());
        assertEquals(units - 1, cartItemCaptor.getValue().getContent().get(productId));
    }

    @Test
    public void getProductsInCart() {
        when(cartRepository.findById(sessionId)).thenReturn(cartItemResponse());
        when(inventoryService.getSingleProduct(productId)).thenReturn(inventoryResponse());

        List<Product> products = cartService.getProductsInCart(TestData.sessionId);

        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals(productId, products.get(0).getId());
        assertEquals(units, products.get(0).getUnits());
    }
}
