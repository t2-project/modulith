package de.unistuttgart.t2.modulith.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.t2.modulith.TestData;
import de.unistuttgart.t2.modulith.cart.CartService;
import de.unistuttgart.t2.modulith.inventory.InventoryService;
import de.unistuttgart.t2.modulith.order.web.SagaRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static de.unistuttgart.t2.modulith.TestData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//@SpringJUnitConfig(TestContext.class)
@ActiveProfiles("test")
public class OrderServiceTest {

    @InjectMocks
    OrderService orderService;

    @Mock
    CartService cartService;

    @Mock
    InventoryService inventoryService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testConfirmOrder() throws JsonProcessingException, OrderNotPlacedException {

        SagaRequest request = new SagaRequest(TestData.sessionId, "cardNumber", "cardOwner", "checksum", 42.0);
        System.out.println(mapper.writeValueAsString(request));

        when(cartService.getCart(sessionId)).thenReturn(cartResponse());
        when(inventoryService.getSingleProduct(productId)).thenReturn(inventoryResponse());

        // what i actually want : verify request to orchestrator
//        mockServer.expect(ExpectedCount.once(), requestTo(JSONs.orchestratorUrl)).andExpect(method(HttpMethod.POST))
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$.cardNumber").value("cardNumber"))
//            .andExpect(jsonPath("$.cardOwner").value("cardOwner"))
//            .andExpect(jsonPath("$.checksum").value("checksum"))
//            .andExpect(jsonPath("$.sessionId").value(JSONs.sessionId))
//            .andExpect(jsonPath("$.total").value(42))
//            .andExpect(content().json(mapper.writeValueAsString(request))).andRespond(withStatus(HttpStatus.OK));
        // TODO when orchestrator then

        // execute
        orderService.confirmOrder(request.getSessionId(), request.getCardNumber(), request.getCardOwner(),
            request.getChecksum());

        verify(cartService, atLeast(1)).getCart(sessionId);
        verify(cartService, times(1)).deleteCart(sessionId);
    }
}
