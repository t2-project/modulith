package de.unistuttgart.t2.modulith.payment;

import de.unistuttgart.t2.modulith.payment.internal.PaymentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Contacts a payment provider, e.g. some credit institute, to execute the payment.
 *
 * @author maumau
 */
@Service
public class PaymentService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Value("${t2.payment.provider.dummy.url}")
    protected String providerUrl;

    @Value("${t2.payment.provider.timeout:10}")
    public int timeout;

    private final RestTemplate template;

    @Autowired
    public PaymentService(RestTemplateBuilder restTemplateBuilder) {
        this.template = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(timeout))
            .setReadTimeout(Duration.ofSeconds(timeout)).build();
    }

    // retry stuff
    // TODO Implement retry
//    RetryConfig config = RetryConfig.custom().maxAttempts(2).build();
//    RetryRegistry registry = RetryRegistry.of(config);
//    Retry retry = registry.retry("paymentRetry");

    public void doPayment(String cardNumber, String cardOwner, String checksum, double total) {
        PaymentData paymentData = new PaymentData(cardNumber, cardOwner, checksum, total);
        // TODO post should be part of a retry
        template.postForObject(providerUrl, paymentData, Void.class);
    }
}
