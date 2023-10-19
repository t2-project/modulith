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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrderIntegrationTests {

    String orderId;

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
        this.orderService = new OrderService(cartService, inventoryService, paymentService, orderRepository);

        orderId = orderRepository.save(new OrderItem("sessionId")).getOrderId();
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
        // execute
        orderService.rejectOrder(orderId);

        // assert
        assertTrue(orderRepository.existsById(orderId));
        assertTrue(orderRepository.findById(orderId).isPresent());

        OrderItem item = orderRepository.findById(orderId).get();
        assertEquals("sessionId", item.getSessionId());
        assertEquals(OrderStatus.FAILURE, item.getStatus());
    }
}
