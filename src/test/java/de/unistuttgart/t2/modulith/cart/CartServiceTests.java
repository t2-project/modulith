package de.unistuttgart.t2.modulith.cart;

import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

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
    public void getCart() {
        when(cartRepository.findById(sessionId)).thenReturn(cartItemResponse());

        Optional<CartContent> cartContent = cartService.getCart(sessionId);

        assertNotNull(cartContent);
        assertEquals(1, cartContent.get().getContent().size());
        assertEquals(units, cartContent.get().getContent().get(productId));
    }
}
