package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.cart.CartContent;
import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.order.repository.OrderItem;
import de.unistuttgart.t2.modulith.order.repository.OrderRepository;
import de.unistuttgart.t2.modulith.order.repository.OrderStatus;
import de.unistuttgart.t2.modulith.payment.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Service;

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

    // TODO implement saga or make it as a transaction
    /**
     * Posts a request to start a transaction to the orchestrator. Attempts to delete the cart of the given sessionId
     * once the orchestrator accepted the request. Nothing happens if the deletion of a cart fails, as the cart service
     * supposed to periodically remove out dated cart entries anyway.
     *
     * @param sessionId  identifies the session
     * @param cardNumber part of payment details
     * @param cardOwner  part of payment details
     * @param checksum   part of payment details
     * @throws OrderNotPlacedException if the order to confirm is empty/ would result in a negative sum
     */
    public void confirmOrder(String sessionId, String cardNumber, String cardOwner, String checksum)
        throws OrderNotPlacedException {

        // TODO is it more reasonable to get total from cart service?
        // Or is it more reasonable to pass the total from the front end
        // (where it was displayed and therefore is known) ??
        double total = getTotal(sessionId);

        if (total <= 0) {
            throw new OrderNotPlacedException(String
                .format("No Order placed for session %s. Cart is either empty or not available. ", sessionId));
        }

        String orderId = createOrder(sessionId);
        LOG.info("order {} created for session {}.", orderId, sessionId);

        paymentService.doPayment(cardNumber, cardOwner, checksum, total);
        inventoryService.commitReservations(sessionId);
        cartService.deleteCart(sessionId);

        LOG.info("deleted cart for session {}.", sessionId);
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
