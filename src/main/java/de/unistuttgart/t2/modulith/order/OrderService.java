package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartContent;
import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.order.repository.OrderItem;
import de.unistuttgart.t2.modulith.order.repository.OrderRepository;
import de.unistuttgart.t2.modulith.order.repository.OrderStatus;
import de.unistuttgart.t2.modulith.order.web.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.payment.PaymentFailedException;
import de.unistuttgart.t2.modulith.payment.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * creates and updates orders.
 *
 * @author maumau
 */
@Service
@EnableMongoRepositories(basePackageClasses = OrderRepository.class)
public class OrderService {

    private final CartService cartService;

    private final InventoryService inventoryService;

    private final PaymentService paymentService;

    private final OrderRepository orderRepository;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public OrderService(@Autowired CartService cartService,
                        @Autowired InventoryService inventoryService,
                        @Autowired PaymentService paymentService,
                        @Autowired OrderRepository orderRepository) {
        this.cartService = cartService;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
    }

    /**
     * create a new Order and save it to the repository. the status of the new order is {@link OrderStatus#SUCCESS
     * SUCCESS}.
     *
     * @param sessionId id of session to create order for
     * @return orderId of created order
     */
    public String createOrder(String sessionId) {

        OrderItem item = new OrderItem(sessionId);
        return orderRepository.save(item).getOrderId();
    }

    /**
     * Set the state of an order to {@link OrderStatus#FAILURE FAILURE}. This operation is idempotent, as a order may
     * never change from {@link OrderStatus#FAILURE FAILURE} to any other status.
     *
     * @param orderId id of order that is to be rejected
     * @throws NoSuchElementException if the id is in the db but retrieval fails anyway.
     */
    public void rejectOrder(String orderId) {

        OrderItem item = orderRepository.findById(orderId).get();
        item.setStatus(OrderStatus.FAILURE);
        orderRepository.save(item);
    }

    // TODO implement transaction

    /**
     * Completes the order by creating an order in the database, making the payment, committing the reservation
     * and finally deleting the cart. Everything, except the deleting of the cart, happens in a transaction.
     * It is not a problem if there is a problem deleting the cart, as out dated carts are periodically deleted anyway.
     *
     * @param sessionId  identifies the session
     * @param cardNumber part of payment details
     * @param cardOwner  part of payment details
     * @param checksum   part of payment details
     * @return orderId   identifies the order
     * @throws OrderNotPlacedException if the order to confirm is empty/ would result in a negative sum
     */
    @Transactional(rollbackFor = {OrderNotPlacedException.class})
    public String confirmOrder(String sessionId, String cardNumber, String cardOwner, String checksum)
        throws OrderNotPlacedException {

        // Calculating total
        double total;
        try {
            // TODO is it more reasonable to get total from cart service?
            // Or is it more reasonable to pass the total from the front end
            // (where it was displayed and therefore is known) ??
            total = getTotal(sessionId);
        } catch (RuntimeException e) {
            throw new OrderNotPlacedException(String
                .format("No order placed for session %s. Calculating total failed.", sessionId), e);
        }
        if (total <= 0) {
            throw new OrderNotPlacedException(String
                .format("No order placed for session %s. Cart is either empty or not available.", sessionId));
        }

        // Create order
        String orderId;
        try {
            orderId = createOrder(sessionId);
            LOG.info("order {} created for session {}.", orderId, sessionId);
        } catch (RuntimeException e) {
            throw new OrderNotPlacedException(String
                .format("No order placed for session %s. Creating order failed.", sessionId), e);
        }

        // Do payment
        try {
            paymentService.doPayment(cardNumber, cardOwner, checksum, total);
        } catch (PaymentFailedException e) {
            throw new OrderNotPlacedException(String.format("Payment for order %s failed", orderId));
        }

        // Commit reservations
        try {
            inventoryService.commitReservations(sessionId);
        } catch (RuntimeException e) {
            throw new OrderNotPlacedException(String
                .format("No order placed for session %s. Committing reservations failed.", sessionId), e);
        }

        // Delete cart
        try {
            cartService.deleteCart(sessionId);
            LOG.info("deleted cart for session {}.", sessionId);
        } catch (RuntimeException e) {
            LOG.warn(String.format("Deleting of cart for session %s failed.", sessionId), e);
        }
        return orderId;
    }

    /**
     * Calculates the total of a users cart.
     * <p>
     * Depends on the cart service to get the cart content and depends on the inventory service to get the price per
     * unit. If either of them fails, the returned total is 0. This is because the store cannot handle partial orders.
     * Its either ordering all items in the cart or none.
     *
     * @param sessionId identifies the session to get total for
     * @return the total money to pay for products in the cart
     */
    private double getTotal(String sessionId) {
        CartContent cart = cartService.getCart(sessionId).orElse(new CartContent());

        double total = 0;

        for (String productId : cart.getProductIds()) {
            Optional<Product> product = inventoryService.getSingleProduct(productId);
            if (product.isEmpty()) {
                return 0;
            }
            total += product.get().getPrice() * cart.getUnits(productId);
        }
        return total;
    }
}
