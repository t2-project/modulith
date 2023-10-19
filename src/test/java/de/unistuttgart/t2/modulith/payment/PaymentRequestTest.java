package de.unistuttgart.t2.modulith.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Test whether Payment service makes requests and retries as it should.
 *
 * @author maumau
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PaymentRequestTest {

    @Autowired
    PaymentService service;

    @Autowired
    private RestTemplate template;

    private MockRestServiceServer mockServer;
    private final String testurl = "http://foo.bar/pay";

//    SagaData data = new SagaData("cardNumber", "cardOwner", "checksum", "sessionId", 1234.5);

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(template);
        service.providerUrl = testurl;
    }

    // TODO Payment tests
//    @Test
//    public void testRequestRetry() {
//        mockServer.expect(ExpectedCount.twice(), requestTo(testurl))
//            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
//
//        // execute
//        assertThrows(InternalServerError.class, () -> {
//            service.handleSagaAction(data);
//        });
//        mockServer.verify();
//    }
//
//    @Test
//    public void testRequest() throws Exception {
//        mockServer.expect(ExpectedCount.once(), requestTo(testurl)).andExpect(method(HttpMethod.POST))
//            .andRespond(withStatus(HttpStatus.OK));
//
//        // execute
//        service.handleSagaAction(data);
//        mockServer.verify();
//    }
}
