package de.unistuttgart.t2.modulith.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.order.repository.OrderItem;
import de.unistuttgart.t2.modulith.order.repository.OrderRepository;
import de.unistuttgart.t2.modulith.payment.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrderServiceTests {

    @InjectMocks
    OrderService orderService;

    @Mock
    CartService cartService;

    @Mock
    InventoryService inventoryService;

    @Mock
    PaymentService paymentService;

    @Mock
    OrderRepository orderRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void confirmOrder() throws JsonProcessingException, OrderNotPlacedException {

//        SagaRequest request = new SagaRequest(TestData.sessionId, "cardNumber", "cardOwner", "checksum", 42.0);
//        System.out.println(mapper.writeValueAsString(request));

        when(cartService.getCart(sessionId)).thenReturn(cartResponse());
        when(inventoryService.getSingleProduct(productId)).thenReturn(inventoryResponse());
        when(orderRepository.save(any())).thenReturn(new OrderItem(sessionId));
//        when(paymentService.doPayment("cardNumber", "cardOwner", "checksum", 42.0)).thenReturn()

        // what i actually want : verify request to orchestrator
//        mockServer.expect(ExpectedCount.once(), requestTo(JSONs.orchestratorUrl)).andExpect(method(HttpMethod.POST))
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$.cardNumber").value("cardNumber"))
//            .andExpect(jsonPath("$.cardOwner").value("cardOwner"))
//            .andExpect(jsonPath("$.checksum").value("checksum"))
//            .andExpect(jsonPath("$.sessionId").value(JSONs.sessionId))
//            .andExpect(jsonPath("$.total").value(42))
//            .andExpect(content().json(mapper.writeValueAsString(request))).andRespond(withStatus(HttpStatus.OK));

        // execute
//        orderService.confirmOrder(request.getSessionId(), request.getCardNumber(), request.getCardOwner(),
//            request.getChecksum());
        orderService.confirmOrder(sessionId, "cardNumber", "cardOwner", "checksum");

        verify(cartService, atLeast(1)).getCart(sessionId);
        verify(cartService, times(1)).deleteCart(sessionId);
        verify(paymentService, times(1)).doPayment(anyString(), anyString(), anyString(), anyDouble());
        verify(inventoryService, times(1)).commitReservations(sessionId);
    }
}
