package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.order.repository.OrderItem;
import de.unistuttgart.t2.modulith.order.repository.OrderRepository;
import de.unistuttgart.t2.modulith.order.web.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.payment.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrderServiceTests {

    @InjectMocks
    OrderService orderService;

    @Mock
    CartService cartService;

    @Mock
    InventoryService inventoryService;

    @Mock
    PaymentService paymentService;

    @Mock
    OrderRepository orderRepository;

    @Test
    public void confirmOrder() throws OrderNotPlacedException {

        when(cartService.getCart(sessionId)).thenReturn(cartResponse());
        when(inventoryService.getSingleProduct(productId)).thenReturn(inventoryResponse());
        when(orderRepository.save(any())).thenReturn(new OrderItem(sessionId));

        orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");

        verify(cartService, atLeast(1)).getCart(sessionId);
        verify(cartService, times(1)).deleteCart(sessionId);
        verify(paymentService, times(1)).doPayment(anyString(), anyString(), anyString(), anyDouble());
        verify(inventoryService, times(1)).commitReservations(sessionId);
    }
}
