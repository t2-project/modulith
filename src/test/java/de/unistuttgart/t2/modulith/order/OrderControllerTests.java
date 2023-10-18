package de.unistuttgart.t2.modulith.order;

import de.unistuttgart.t2.modulith.order.web.OrderController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

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
}
