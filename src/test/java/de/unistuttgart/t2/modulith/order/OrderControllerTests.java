package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.order.web.OrderController;
import de.unistuttgart.t2.modulith.order.web.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.order.web.OrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test the logic of {@link OrderController}.
 *
 * @author maumau
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrderControllerTests {

    @Mock
    OrderService orderService;

    OrderController controller;

    @BeforeEach
    public void setUp() {
        controller = new OrderController(orderService);
    }

    @Test
    public void confirmOrder() throws OrderNotPlacedException {

        OrderRequest request = new OrderRequest("cardNumber", "cardOwner", "checksum", "sessionId");
        controller.confirmOrder(request);

        verify(orderService, times(1)).confirmOrder(anyString(), anyString(), anyString(), anyString());
    }
}
