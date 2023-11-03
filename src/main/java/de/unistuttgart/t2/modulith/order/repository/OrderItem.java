package de.unistuttgart.t2.modulith.order.repository;

import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.Date;

/**
 * Represents an order.<br>
 * Each order has a unique {@code orderId} to find it in the repository, a {@code sessionId} to correlate it to other
 * things and a timestamp.
 *
 * @author maumau
 */
public class OrderItem {

    @Id
    private String orderId;

    private String sessionId;
    private OrderStatus status;
    private Date timestamp;

    /**
     * used (and required) by Spring.
     */
    public OrderItem() {
    }

    /**
     * Create a new order.<br>
     * There is no {@code orderId} because it gets set by repository.<br>
     * {@code status} of a new order is always {@link OrderStatus#SUCCESS}, {@code timestamp} is always the current
     * time.
     *
     * @param sessionId the session ID of the user making this request
     */
    public OrderItem(String sessionId) {
        this.sessionId = sessionId;
        status = OrderStatus.SUCCESS;
        timestamp = Date.from(Instant.now());
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("%s : %s , %s ", orderId, sessionId, timestamp.toString());
    }
}
