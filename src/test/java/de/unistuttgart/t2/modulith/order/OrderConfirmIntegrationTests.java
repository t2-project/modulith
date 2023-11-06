package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.exceptions.InsufficientUnitsAvailableException;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryItem;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryRepository;
import de.unistuttgart.t2.modulith.inventory.repository.ReservationRepository;
import de.unistuttgart.t2.modulith.order.repository.OrderRepository;
import de.unistuttgart.t2.modulith.order.repository.OrderStatus;
import de.unistuttgart.t2.modulith.payment.PaymentFailedException;
import de.unistuttgart.t2.modulith.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static de.unistuttgart.t2.modulith.TestData.sessionId;
import static de.unistuttgart.t2.modulith.TestData.units;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrderConfirmIntegrationTests {

    private OrderService orderService;
    private CartService cartService;
    private InventoryService inventoryService;

    @Mock
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private String productId;

    @BeforeEach
    public void setup() throws InsufficientUnitsAvailableException {

        // Spying on InventoryService, CartService and OrderService is needed to be able to throw exceptions for specific test cases
        this.inventoryService = spy(new InventoryService(inventoryRepository, reservationRepository));
        this.cartService = spy(new CartService(cartRepository));
        this.orderService = spy(new OrderService(cartService, inventoryService, paymentService, orderRepository));

        orderRepository.deleteAll();
        cartRepository.deleteAll();
        reservationRepository.deleteAll();
        inventoryRepository.deleteAll();

        InventoryItem savedInventoryItem = inventoryRepository.save(
            new InventoryItem(null, "productName", "description", units, 42.5));
        productId = savedInventoryItem.getId();
        cartRepository.save(new CartItem(sessionId, Map.of(productId, units)));
        inventoryService.makeReservation(sessionId, productId, units);

        // assert repository status before tests
        assertEquals(0, orderRepository.findAll().size(), "Expected no order in repository");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected 1 cart item in repository");
        assertEquals(1, inventoryRepository.findAll().size(), "Expected 1 inventory item in repository");
        assertEquals(1, inventoryRepository.findAll().get(0).getReservations().size(), "Expected inventory item includes one reservation item");
        assertEquals(units, inventoryRepository.findAll().get(0).getUnits(), "Expected there are units available");
        assertEquals(1, reservationRepository.findAll().size(), "Expected 1 reservation entry in repository");
    }

    @Test
    public void confirmOrder_Succeeds() throws Exception {

        // execute
        String id = orderService.confirmOrder(
            sessionId, "cardNumber", "cardOwner", "checksum");

        // assert
        assertTrue(orderRepository.findById(id).isPresent(), "Expected order is created in database");
        assertFalse(cartRepository.findById(sessionId).isPresent(), "Expected cart to be deleted");
        assertEquals(0, reservationRepository.findAll().size(), "Expected reservations are committed");
        verify(paymentService, times(1)).doPayment(anyString(), anyString(), anyString(), anyDouble());
        verify(cartService, times(1)).deleteCart(sessionId);
    }

    @Test
    public void confirmOrder_CalculatingTotalFails_OrderIsNotPlaced() throws PaymentFailedException {

        // setup
        when(inventoryService.getSingleProduct(productId)).thenThrow(new RuntimeException("runtime error"));

        // execute
        Exception actualException = assertThrows(Exception.class,
            () -> orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum"));

        // assert
        assertNotNull(actualException);
        assertTrue(actualException.getMessage().contains("total"),
            String.format("Actual exception message: %s", actualException.getMessage()));
        assertEquals(0, orderRepository.findAll().size(), "Expected no order in database");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected cart still be in database");
        assertEquals(1, reservationRepository.findAll().size(), "Expected reservations are still in database");
        verify(paymentService, never()).doPayment(anyString(), anyString(), anyString(), anyDouble());
    }

    @Test
    public void confirmOrder_CartIsEmpty_OrderIsNotPlaced() throws PaymentFailedException {

        // setup
        cartRepository.deleteAll();

        // execute
        Exception actualException = assertThrows(Exception.class,
            () -> orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum"));

        // assert
        assertNotNull(actualException);
        assertTrue(actualException.getMessage().contains("Cart"),
            String.format("Actual exception message: %s", actualException.getMessage()));
        assertEquals(0, orderRepository.findAll().size(), "Expected no order in database");
        assertEquals(1, reservationRepository.findAll().size(), "Expected reservations are still in database");
        verify(paymentService, never()).doPayment(anyString(), anyString(), anyString(), anyDouble());

    }

    @Test
    public void confirmOrder_PaymentFails_OrderIsNotPlaced() throws PaymentFailedException {

        // setup
        doThrow(new PaymentFailedException("payment failed"))
            .when(paymentService).doPayment(anyString(), anyString(), anyString(), anyDouble());

        // execute
        Exception actualException = assertThrows(Exception.class,
            () -> orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum"));

        // assert
        assertNotNull(actualException);
        assertTrue(actualException.getMessage().contains("Payment"),
            String.format("Actual exception message: %s", actualException.getMessage()));
        assertEquals(1, orderRepository.findAll().size(), "Expected order in database");
        assertEquals(OrderStatus.FAILURE, orderRepository.findAll().get(0).getStatus(), "Expected order status to be set to FAILURE");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected cart still be in database");

        assertEquals(1, reservationRepository.findAll().size(), "Expected reservations were not committed");
        assertEquals(units, inventoryRepository.findById(productId).get().getUnits(), "Expected reservations were not committed");
    }
}
