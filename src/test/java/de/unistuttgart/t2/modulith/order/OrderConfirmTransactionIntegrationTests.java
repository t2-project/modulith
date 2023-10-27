package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryItem;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryRepository;
import de.unistuttgart.t2.modulith.inventory.repository.ReservationRepository;
import de.unistuttgart.t2.modulith.order.repository.OrderRepository;
import de.unistuttgart.t2.modulith.order.repository.OrderStatus;
import de.unistuttgart.t2.modulith.order.web.OrderNotPlacedException;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

import static de.unistuttgart.t2.modulith.TestData.sessionId;
import static de.unistuttgart.t2.modulith.TestData.units;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrderConfirmTransactionIntegrationTests {

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
    @Autowired
    private TransactionTemplate transactionManager;

    private String productId;

    @BeforeEach
    public void setup() {

        // Spying on InventoryService, CartService and OrderService is needed to be able to throw exceptions for specific test cases
        this.inventoryService = spy(new InventoryService(inventoryRepository, reservationRepository));
        this.cartService = spy(new CartService(cartRepository, inventoryService));

        this.orderService = spy(new OrderService(cartService, inventoryService, paymentService, orderRepository, transactionManager));

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
    public void confirmOrder_Succeeds() throws OrderNotPlacedException, PaymentFailedException {

        // execute
        String id = orderService.confirmOrder(
            sessionId, "cardNumber", "cardOwner", "checksum");

        // assert
        assertTrue(orderRepository.findById(id).isPresent(), "Expected order is created in database");
        assertFalse(cartRepository.findById(sessionId).isPresent(), "Expected cart to be deleted");
        assertEquals(0, reservationRepository.findAll().size(), "Expected reservations are committed");
        verify(paymentService, times(1)).doPayment(anyString(), anyString(), anyString(), anyDouble());
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
        assertEquals(OrderNotPlacedException.class, actualException.getClass());
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
        assertEquals(OrderNotPlacedException.class, actualException.getClass());
        assertTrue(actualException.getMessage().contains("Cart"),
            String.format("Actual exception message: %s", actualException.getMessage()));
        assertEquals(0, orderRepository.findAll().size(), "Expected no order in database");
        assertEquals(1, reservationRepository.findAll().size(), "Expected reservations are still in database");
        verify(paymentService, never()).doPayment(anyString(), anyString(), anyString(), anyDouble());

    }

    @Test
    public void confirmOrder_PaymentFails_RollbackIsExecuted() throws PaymentFailedException {

        // setup
        doThrow(new PaymentFailedException("payment failed"))
            .when(paymentService).doPayment(anyString(), anyString(), anyString(), anyDouble());

        // execute
        Exception actualException = assertThrows(Exception.class,
            () -> orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum"));

        // assert
        assertNotNull(actualException);
        assertEquals(OrderNotPlacedException.class, actualException.getClass());
        assertTrue(actualException.getMessage().contains("Payment"),
            String.format("Actual exception message: %s", actualException.getMessage()));
        assertEquals(1, orderRepository.findAll().size(), "Expected order in database");
        assertEquals(OrderStatus.FAILURE, orderRepository.findAll().get(0).getStatus(), "Expected order status to be set to FAILURE");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected cart still be in database");

        assertEquals(1, reservationRepository.findAll().size(), "Expected rollback revert committing of reservations");
        assertEquals(units, inventoryRepository.findById(productId).get().getUnits(), "Expected rollback reset units to original");
    }

    @Test
    public void confirmOrder_CommitReservationsFails_OrderIsSetToFailure() throws PaymentFailedException {

        // setup
        doThrow(new RuntimeException("committing reservations failed")).when(inventoryService).commitReservations(anyString());

        // execute
        Exception actualException = assertThrows(Exception.class,
            () -> orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum"));

        // assert
        assertNotNull(actualException);
        assertEquals(OrderNotPlacedException.class, actualException.getClass());
        assertTrue(actualException.getMessage().contains("reservations"),
            String.format("Actual exception message: %s", actualException.getMessage()));
        assertEquals(1, orderRepository.findAll().size(), "Expected order in database");
        assertEquals(OrderStatus.FAILURE, orderRepository.findAll().get(0).getStatus(), "Expected order status to be set to FAILURE");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected cart still be in database");
        assertEquals(1, reservationRepository.findAll().size(), "Expected reservations are still in database");
        verify(paymentService, never()).doPayment(anyString(), anyString(), anyString(), anyDouble());
    }

    @Test
    public void confirmOrder_DeletingCartFails_OrderIsPlacedNevertheless() throws PaymentFailedException {

        // setup
        doThrow(new RuntimeException("deleting cart failed")).when(cartService).deleteCart(anyString());

        // execute
        String id = orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");

        // assert
        assertNotNull(id, "Expected order id was created");
    }
}
