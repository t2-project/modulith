package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartContent;
import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.order.repository.OrderItem;
import de.unistuttgart.t2.modulith.order.repository.OrderRepository;
import de.unistuttgart.t2.modulith.order.repository.OrderStatus;
import de.unistuttgart.t2.modulith.payment.PaymentFailedException;
import de.unistuttgart.t2.modulith.payment.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Creates and updates orders.
 *
 * @author maumau
 * @author davidkopp
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

    /**
     * Completes the order by creating an order in the database, making the payment, committing the reservation
     * and finally deleting the cart.
     * The method is not marked as transactional on purpose! MongoDB's transaction support is not configured yet,
     * so the create order operation would be not transactional anyway.
     * However, if the payment fails, the order gets rejected. In case of a payment failure, the cart and reservations
     * are not deleted so that a new order attempt can be made.
     *
     * @param sessionId  identifies the session
     * @param cardNumber part of payment details
     * @param cardOwner  part of payment details
     * @param checksum   part of payment details
     * @return identifies the order
     * @throws Exception if the order to confirm is empty, would result in a negative sum
     *                   or if there are any other errors during the placement of the order
     */
    public String confirmOrder(String sessionId, String cardNumber, String cardOwner, String checksum) throws Exception {

        // Calculating total
        double total;
        try {
            total = getTotal(sessionId);
        } catch (Exception e) {
            throw new Exception(String.format("No order placed for session '%s'. Calculating total failed.", sessionId), e);
        }
        if (total <= 0) {
            throw new Exception(String.format("No order placed for session '%s'. Cart is either empty or not available.", sessionId));
        }

        String orderId = createOrder(sessionId);
        LOG.info("Order '{}' created for session '{}'. Waiting for payment...", orderId, sessionId);

        // Do payment
        try {
            paymentService.doPayment(cardNumber, cardOwner, checksum, total);
            LOG.info("Payment of order '{}' was successful!", orderId);
        } catch (PaymentFailedException e) {
            LOG.error("Payment of order '{}' failed! Rejecting order.", orderId);
            rejectOrder(orderId);
            throw new RuntimeException(
                String.format("Payment for order '%s' of session '%s' failed.", orderId, sessionId), e);
        }

        // Commit reservations
        inventoryService.commitReservations(sessionId);

        // Delete cart
        cartService.deleteCart(sessionId);

        LOG.info("Order '{}' executed successfully for session '{}'.", orderId, sessionId);

        return orderId;
    }

    /**
     * Calculates the total of a users cart.
     * <p>
     * Depends on the cart module to get the cart content and depends on the inventory module to get the price per
     * unit.
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
