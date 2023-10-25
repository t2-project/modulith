package de.unistuttgart.t2.modulith.payment;

import de.unistuttgart.t2.modulith.payment.internal.PaymentData;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Contacts a payment provider, e.g. some credit institute, to execute the payment.
 *
 * @author maumau
 */
@Service
public class PaymentService {

    @Value("${t2.payment.provider.dummy.url}")
    protected String providerUrl;

    @Value("${t2.payment.provider.timeout:10}")
    public int timeout;

    private final RestTemplate template;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    // retry stuff
    RetryConfig config = RetryConfig.custom().maxAttempts(2).build();
    RetryRegistry registry = RetryRegistry.of(config);
    Retry retry = registry.retry("paymentRetry");

    public PaymentService() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        this.template = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(timeout))
            .setReadTimeout(Duration.ofSeconds(timeout)).build();
    }

    public PaymentService(RestTemplate restTemplate) {
        this.template = restTemplate;
    }

    public void doPayment(String cardNumber, String cardOwner, String checksum, double total) throws PaymentFailedException {
        try {
            PaymentData paymentData = new PaymentData(cardNumber, cardOwner, checksum, total);
            Retry.decorateSupplier(retry, () -> template.postForObject(providerUrl, paymentData, Void.class)).get();
        } catch (
            RestClientException e) {
            LOG.error(e.getMessage(), e);
            throw new PaymentFailedException("Payment failed", e);
        }
    }
}
