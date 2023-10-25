package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryItem;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryRepository;
import de.unistuttgart.t2.modulith.inventory.repository.Reservation;
import de.unistuttgart.t2.modulith.inventory.repository.ReservationRepository;
import de.unistuttgart.t2.modulith.order.repository.OrderRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static de.unistuttgart.t2.modulith.TestData.sessionId;
import static de.unistuttgart.t2.modulith.TestData.units;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Transactional
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

    private String productId;

    @BeforeEach
    public void setup() {

        this.inventoryService = new InventoryService(inventoryRepository, reservationRepository);
        this.cartService = new CartService(cartRepository, inventoryService);
        this.orderService = new OrderService(cartService, inventoryService, paymentService, orderRepository);

        orderRepository.deleteAll();
        cartRepository.deleteAll();
        reservationRepository.deleteAll();
        inventoryRepository.deleteAll();

        InventoryItem savedInventoryItem = inventoryRepository.save(new InventoryItem(null, "productName", "description", units, 42.5));
        productId = savedInventoryItem.getId();
        cartRepository.save(new CartItem(sessionId, Map.of(productId, units)));
        reservationRepository.save(new Reservation(units, sessionId, savedInventoryItem));

        // assert repository status before tests
        assertEquals(0, orderRepository.findAll().size(), "Expected no order in repository");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected 1 cart item in repository");
        assertEquals(1, reservationRepository.findAll().size(), "Expected 1 reservation entry in repository ");
    }

    @Test
    public void confirmOrderSucceeds() throws OrderNotPlacedException, PaymentFailedException {

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

        // Setup
        when(inventoryService.getSingleProduct(productId)).thenThrow(new RuntimeException("runtime error"));

        // execute
        Exception actualException = null;
        try {
            orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");
        } catch (OrderNotPlacedException e) {
            actualException = e;
        }

        // assert
        assertNotNull(actualException);
        assertEquals(actualException.getClass(), OrderNotPlacedException.class);
        assertTrue(actualException.getMessage().contains("total"));
        assertEquals(0, orderRepository.findAll().size(), "Expected no order in database");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected cart still be in database");
        assertEquals(1, reservationRepository.findAll().size(), "Expected reservations are still in database");
        verify(paymentService, never()).doPayment(anyString(), anyString(), anyString(), anyDouble());
    }

    @Test
    public void confirmOrder_CartIsEmpty_OrderIsNotPlaced() throws PaymentFailedException {

        // Setup
        cartRepository.deleteAll();

        // execute
        Exception actualException = null;
        try {
            orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");
        } catch (OrderNotPlacedException e) {
            actualException = e;
        }

        // assert
        assertNotNull(actualException);
        assertEquals(actualException.getClass(), OrderNotPlacedException.class);
        assertTrue(actualException.getMessage().contains("Cart"));
        assertEquals(0, orderRepository.findAll().size(), "Expected no order in database");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected cart still be in database");
        assertEquals(1, reservationRepository.findAll().size(), "Expected reservations are still in database");
        verify(paymentService, never()).doPayment(anyString(), anyString(), anyString(), anyDouble());

    }

    @Test
    public void confirmOrder_CreatingOrderFails_OrderIsNotPlaced() throws PaymentFailedException {

        // setup
        doThrow(new RuntimeException("runtime error")).when(orderRepository).save(any());

        // execute
        Exception actualException = null;
        try {
            orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");
        } catch (OrderNotPlacedException e) {
            actualException = e;
        }

        // assert
        assertNotNull(actualException);
        assertEquals(actualException.getClass(), OrderNotPlacedException.class);
        assertTrue(actualException.getMessage().contains("order failed"));
        assertEquals(0, orderRepository.findAll().size(), "Expected no order in database");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected cart still be in database");
        assertEquals(1, reservationRepository.findAll().size(), "Expected reservations are still in database");
        verify(paymentService, never()).doPayment(anyString(), anyString(), anyString(), anyDouble());
    }

    @Test
    public void confirmOrder_PaymentFails_OrderIsNotPlaced() throws PaymentFailedException {

        // Setup
        doThrow(new PaymentFailedException("payment failed"))
            .when(paymentService).doPayment(anyString(), anyString(), anyString(), anyDouble());

        // execute
        Exception actualException = null;
        try {
            orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");
        } catch (OrderNotPlacedException e) {
            actualException = e;
        }

        // assert
        assertNotNull(actualException);
        assertEquals(actualException.getClass(), OrderNotPlacedException.class);
        assertTrue(actualException.getMessage().contains("payment"));
        assertEquals(0, orderRepository.findAll().size(), "Expected no order in database");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected cart still be in database");
        assertEquals(1, reservationRepository.findAll().size(), "Expected reservations are still in database");
    }

    @Test
    public void confirmOrder_CommitReservationsFails_OrderIsNotPlaced() throws PaymentFailedException {

        // setup
        doThrow(new RuntimeException("committing reservations failed")).when(inventoryService).commitReservations(anyString());

        // execute
        Exception actualException = null;
        try {
            orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");
        } catch (OrderNotPlacedException e) {
            actualException = e;
        }

        // assert
        assertNotNull(actualException);
        assertEquals(actualException.getClass(), OrderNotPlacedException.class);
        assertTrue(actualException.getMessage().contains("order failed"));
        assertEquals(0, orderRepository.findAll().size(), "Expected no order in database");
        assertTrue(cartRepository.findById(sessionId).isPresent(), "Expected cart still be in database");
        assertEquals(1, reservationRepository.findAll().size(), "Expected reservations are still in database");
        verify(paymentService, never()).doPayment(anyString(), anyString(), anyString(), anyDouble());
    }

    @Test
    public void confirmOrder_DeletingCartFails_OrderIsPlacedNevertheless() throws PaymentFailedException {

        // setup
        doThrow(new RuntimeException("deleting cart failed")).when(cartService).deleteCart(anyString());

        // execute
        Exception actualException = null;
        String id = null;
        try {
            id = orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");
        } catch (OrderNotPlacedException e) {
            actualException = e;
        }

        // assert
        assertNull(actualException, "Expected no error thrown");
        assertNotNull(id, "Expected order id was created");
    }
}
