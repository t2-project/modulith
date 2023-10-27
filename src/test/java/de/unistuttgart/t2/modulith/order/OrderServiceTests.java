package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.order.repository.OrderItem;
import de.unistuttgart.t2.modulith.order.repository.OrderRepository;
import de.unistuttgart.t2.modulith.order.web.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.payment.PaymentFailedException;
import de.unistuttgart.t2.modulith.payment.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

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

    @Mock
    private TransactionTemplate transactionTemplate;

    @Test
    public void confirmOrderSucceeds() throws OrderNotPlacedException, PaymentFailedException {

        when(cartService.getCart(sessionId)).thenReturn(cartResponse());
        when(inventoryService.getSingleProduct(productId)).thenReturn(inventoryResponse());
        when(orderRepository.save(any())).thenReturn(new OrderItem(sessionId));
        when(transactionTemplate.execute(any())).thenAnswer(invocation ->
            invocation.<TransactionCallback<String>>getArgument(0).doInTransaction(new SimpleTransactionStatus()));

        orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");

        verify(cartService, atLeast(1)).getCart(sessionId);
        verify(cartService, times(1)).deleteCart(sessionId);
        verify(paymentService, times(1)).doPayment(anyString(), anyString(), anyString(), anyDouble());
        verify(inventoryService, times(1)).commitReservations(sessionId);
    }
}
