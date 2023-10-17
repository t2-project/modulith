package de.unistuttgart.t2.modulith;

import de.unistuttgart.t2.modulith.cart.CartService;
import group.msg.jpowermonitor.junit.JPowerMonitorExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({JPowerMonitorExtension.class})
@SpringBootTest
@ActiveProfiles("test")
public class PowerMeasurementTests {

    private CartService cartService;

    @BeforeEach
    public void populateRepository(@Autowired CartService cartService) {
        this.cartService = cartService;
    }

    @RepeatedTest(3)
    public void getCartFromDatabase() {
        // make request
        for (int i = 0; i < 10000; i++) {
            cartService.getCart("foo");
        }

        // assert
        assertNull(null);
    }
}
