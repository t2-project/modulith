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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * creates and updates orders.
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

    private final TransactionTemplate transactionTemplate;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public OrderService(@Autowired CartService cartService,
                        @Autowired InventoryService inventoryService,
                        @Autowired PaymentService paymentService,
                        @Autowired OrderRepository orderRepository,
                        @Autowired TransactionTemplate transactionTemplate) {
        this.cartService = cartService;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
        this.transactionTemplate = transactionTemplate;
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
     * and finally deleting the cart. Everything, except the deleting of the cart, happens in a transaction.
     * It is not a problem if there is a problem deleting the cart, as out dated carts are periodically deleted anyway.
     *
     * @param sessionId  identifies the session
     * @param cardNumber part of payment details
     * @param cardOwner  part of payment details
     * @param checksum   part of payment details
     * @return identifies the order
     * @throws OrderNotPlacedException if the order to confirm is empty/ would result in a negative sum
     */
    public String confirmOrder(String sessionId, String cardNumber, String cardOwner, String checksum)
        throws OrderNotPlacedException {

        // Calculating total
        double total;
        try {
            // TODO is it more reasonable to get total from cart module?
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

        // TODO Make create order part of the transaction (MongoDB transaction required)
        // It is currently not part of the transaction, because the required configuration for the MongoDB is missing.
        String orderId = createOrder(sessionId);
        LOG.info("order {} created for session {}.", orderId, sessionId);

        // JPA Transaction for payment and committing reservation
        return transactionTemplate.execute(transactionStatus -> {

            // Commit reservations
            try {
                inventoryService.commitReservations(sessionId);
            } catch (RuntimeException e) {
                rejectOrder(orderId);
                transactionStatus.setRollbackOnly();
                throw new OrderNotPlacedException(
                    String.format("Committing reservations for order %s of session %s failed. Order is rejected.", orderId, sessionId), e);
            }

            // Do payment
            try {
                paymentService.doPayment(cardNumber, cardOwner, checksum, total);
            } catch (PaymentFailedException e) {
                LOG.warn("Payment failed");
                rejectOrder(orderId);
                transactionStatus.setRollbackOnly();
                throw new OrderNotPlacedException(
                    String.format("Payment for order %s of session %s failed. Order is rejected.", orderId, sessionId), e);
            }

            // Delete cart
            try {
                cartService.deleteCart(sessionId);
                LOG.info("deleted cart for session {}.", sessionId);
            } catch (RuntimeException e) {
                // Runtime exception is no problem for the transaction, because the cart will be cleared eventually.
                LOG.warn(String.format("Deleting of cart for session %s failed.", sessionId), e);
            }
            return orderId;
        });
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
