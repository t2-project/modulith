package de.unistuttgart.t2.modulith.order.web;

import de.unistuttgart.t2.modulith.inventory.ReservationFailedException;
import de.unistuttgart.t2.modulith.order.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(@Autowired OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Place an order, i.e. start a transaction.<br>
     * Upon successfully placing the order, the cart is cleared and the session gets invalidated.<br>
     * If the user wants to place another order he needs a new http session.
     *
     * @param request request to place an Order
     * @throws OrderNotPlacedException if the order could not be placed.
     */
    @Operation(summary = "Order all items in the cart", description = "Order all items in the cart")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Order for items is placed"),
        @ApiResponse(responseCode = "500", description = "Order could not be placed")})
    @PostMapping("/confirm")
    public void confirmOrder(@RequestBody OrderRequest request)
        throws OrderNotPlacedException {
        orderService.confirmOrder(request.getSessionId(), request.getCardNumber(), request.getCardOwner(),
            request.getChecksum());
    }

    /**
     * Creates the response entity if a request could not be served because a custom exception was thrown.
     *
     * @param exception the exception that was thrown
     * @return a response entity with an exceptional message
     */
    @ExceptionHandler({ OrderNotPlacedException.class, ReservationFailedException.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleOrderNotPlacedException(OrderNotPlacedException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }
}
