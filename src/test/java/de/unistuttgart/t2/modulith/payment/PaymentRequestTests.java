package de.unistuttgart.t2.modulith.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/**
 * Test whether Payment service makes requests and retries as it should.
 *
 * @author maumau
 */
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(classes = TestContext.class)
public class PaymentRequestTests {

    @Autowired
    PaymentService service;

    @Autowired
    private RestTemplate template;

    private MockRestServiceServer mockServer;

    private final String testUrl = "http://foo.bar/pay";

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(template);
        service.providerUrl = testUrl;
    }

    @Test
    public void testRequestRetry() {
        mockServer.expect(ExpectedCount.twice(), requestTo(testUrl))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // execute
        assertThrows(PaymentFailedException.class, () ->
            service.doPayment("cardNumber", "cardOwner", "checksum", 1234.5));
        mockServer.verify();
    }

    @Test
    public void testRequest() throws PaymentFailedException {
        mockServer.expect(ExpectedCount.once(), requestTo(testUrl)).andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK));

        // execute
        service.doPayment("cardNumber", "cardOwner", "checksum", 1234.5);
        mockServer.verify();
    }
}
