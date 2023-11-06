package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.order.repository.OrderItem;
import de.unistuttgart.t2.modulith.order.repository.OrderRepository;
import de.unistuttgart.t2.modulith.order.repository.OrderStatus;
import de.unistuttgart.t2.modulith.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrderServiceIntegrationTests {

    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Mock
    CartService cartService;

    @Mock
    InventoryService inventoryService;

    @Mock
    PaymentService paymentService;

    @BeforeEach
    public void setup() {
        TransactionTemplate fakeTransactionTemplate = FakeTransactionTemplate.spied();
        this.orderService = new OrderService(cartService, inventoryService, paymentService, orderRepository, fakeTransactionTemplate);
        orderRepository.deleteAll();
    }

    @Test
    public void createOrder() {
        // execute
        String id = orderService.createOrder("sessionId");

        // assert
        assertTrue(orderRepository.existsById(id));
        assertTrue(orderRepository.findById(id).isPresent());

        OrderItem item = orderRepository.findById(id).get();
        assertEquals("sessionId", item.getSessionId());
        assertEquals(OrderStatus.SUCCESS, item.getStatus());
    }

    @Test
    public void rejectOrder() {

        // Setup order repository
        String orderId = orderRepository.save(new OrderItem("sessionId")).getOrderId();

        // execute
        orderService.rejectOrder(orderId);

        // assert
        assertTrue(orderRepository.existsById(orderId));
        assertTrue(orderRepository.findById(orderId).isPresent());

        OrderItem item = orderRepository.findById(orderId).get();
        assertEquals("sessionId", item.getSessionId());
        assertEquals(OrderStatus.FAILURE, item.getStatus());
    }

    @Test
    public void confirmOrder() throws Exception {

        // Setup mocks
        when(cartService.getCart(sessionId)).thenReturn(cartResponse());
        when(inventoryService.getSingleProduct(productId)).thenReturn(inventoryResponse());

        // execute
        String id = orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");

        // assert
        assertTrue(orderRepository.existsById(id));
        assertTrue(orderRepository.findById(id).isPresent());

        OrderItem item = orderRepository.findById(id).get();
        assertEquals(sessionId, item.getSessionId());
        assertEquals(OrderStatus.SUCCESS, item.getStatus());
    }
}
