package de.unistuttgart.t2.modulith.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class PaymentServiceTests {

    @Autowired
    PaymentService paymentService;

    @Autowired
    private RestTemplate template;

    private MockRestServiceServer mockServer;

    @Value("${t2.payment.provider.dummy.url}")
    private String providerUrl;

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(template);
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
