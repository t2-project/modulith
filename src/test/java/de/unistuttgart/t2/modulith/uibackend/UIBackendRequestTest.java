package de.unistuttgart.t2.modulith.uibackend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.t2.modulith.cart.CartModule;
import de.unistuttgart.t2.modulith.common.CartContent;
import de.unistuttgart.t2.modulith.common.ReservationRequest;
import de.unistuttgart.t2.modulith.common.SagaRequest;
import de.unistuttgart.t2.modulith.uibackend.exceptions.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.uibackend.exceptions.ReservationFailedException;
import de.unistuttgart.t2.modulith.uibackend.supplicants.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static de.unistuttgart.t2.modulith.uibackend.supplicants.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Test whether UIBackendservice makes the right requests.
 * <p>
 * The Set up is like this:
 * <ul>
 * <li>Call the operation under test.
 * <li>Uses the mock server to receive the request and verify that it is placed as intended.
 * </ul>
 * 
 * @author maumau
 */
@ExtendWith(MockitoExtension.class)
//@SpringJUnitConfig(TestContext.class)
@ActiveProfiles("test")
public class UIBackendRequestTest {

    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    UIBackendService service;

    @Mock
    CartModule cartModule;

    @Captor
    ArgumentCaptor<CartContent> cartContentCaptor;

    @Captor
    ArgumentCaptor<String> sessionIdCaptor;
    
    @BeforeEach
    public void setUp() {
    }

    // TODO Complete UIBackendRequestTest

    @Test
    public void testConfirmOrder() throws JsonProcessingException, OrderNotPlacedException {

        SagaRequest request = new SagaRequest(TestData.sessionId, "cardNumber", "cardOwner", "checksum", 42.0);
        System.out.println(mapper.writeValueAsString(request));

        // mock cart response
        when(cartModule.getCart(sessionId)).thenReturn(cartResponse());

        // mock inventory response
//        mockServer.expect(ExpectedCount.once(), requestTo(JSONs.inventoryUrl + JSONs.productId))
//            .andExpect(method(HttpMethod.GET))
//            .andRespond(withSuccess(inventoryResponse(), MediaType.APPLICATION_JSON));
        // TODO when inventory then

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
        service.confirmOrder(request.getSessionId(), request.getCardNumber(), request.getCardOwner(),
            request.getChecksum());

        verify(cartModule, atLeast(1)).getCart(sessionId);
        verify(cartModule, times(1)).deleteCart(sessionId);
//        mockServer.verify();
    }

    @Test
    public void testGetSingleProduct() {

//        mockServer.expect(ExpectedCount.once(), requestTo(JSONs.inventoryUrl + JSONs.productId))
//            .andExpect(method(HttpMethod.GET))
//            .andRespond(withSuccess(inventoryResponse(), MediaType.APPLICATION_JSON));
        // TODO when inventory then

        // execute
        service.getSingleProduct(TestData.productId);
//        mockServer.verify();
    }

    @Test
    public void testMakeReservation() throws ReservationFailedException {
        ReservationRequest request = new ReservationRequest(productId, sessionId, 2);

//        mockServer.expect(ExpectedCount.once(), requestTo(reservationUrl)).andExpect(method(HttpMethod.POST))
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(content().json(mapper.writeValueAsString(request)))
//            .andRespond(withSuccess(inventoryResponse(), MediaType.APPLICATION_JSON));
        // TODO Test when reservation then

        // execute
        service.makeReservations(sessionId, productId, 2);
//        mockServer.verify();
    }

    @Test
    public void testGetAllProducts() {

        // twice = once for products and once for page
//        mockServer.expect(ExpectedCount.twice(), requestTo(inventoryUrl)).andExpect(method(HttpMethod.GET))
//            .andRespond(withSuccess(inventoryResponseAllProducts(), MediaType.APPLICATION_JSON));
        // TODO Test when inventory then

        // execute
        service.getAllProducts();
//        mockServer.verify();
    }

    @Test
    public void testAddItemToCart() {
        when(cartModule.getCart(sessionId)).thenReturn(cartResponse());

        // execute
        service.addItemToCart(sessionId, productId, 1);

        verify(cartModule, times(1)).saveCart(sessionIdCaptor.capture(), cartContentCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(units + 1, cartContentCaptor.getValue().getContent().get(productId));
    }

    @Test
    public void testDeleteItemFromCart() {
        when(cartModule.getCart(sessionId)).thenReturn(cartResponse());

        // execute
        service.deleteItemFromCart(sessionId, productId, 1);

        verify(cartModule, times(1)).saveCart(sessionIdCaptor.capture(), cartContentCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(units - 1, cartContentCaptor.getValue().getContent().get(productId));
    }

    @Test
    public void testGetProductsInCart() {
        when(cartModule.getCart(sessionId)).thenReturn(cartResponseMulti());

        // TODO Test when inventory then
//        mockServer.expect(ExpectedCount.once(), requestTo(inventoryUrl + productId)).andExpect(method(HttpMethod.GET))
//            .andRespond(withSuccess(inventoryResponse(), MediaType.APPLICATION_JSON));
//        mockServer.expect(ExpectedCount.once(), requestTo(inventoryUrl + anotherproductId))
//            .andExpect(method(HttpMethod.GET))
//            .andRespond(withSuccess(anotherInventoryResponse(), MediaType.APPLICATION_JSON));

        service.getProductsInCart(sessionId);

//        mockServer.verify();
        verify(cartModule, atLeast(1)).getCart(sessionId);
    }
}
