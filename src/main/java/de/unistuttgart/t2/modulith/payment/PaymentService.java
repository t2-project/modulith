package de.unistuttgart.t2.modulith.payment;

import de.unistuttgart.t2.modulith.payment.domain.PaymentData;
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
 * @author davidkopp
 */
@Service
public class PaymentService {

    @Value("${t2.payment.provider.dummy.url}")
    protected String providerUrl;

    @Value("${t2.payment.provider.timeout:5}")
    public int timeout;

    @Value("${t2.payment.provider.enabled:true}")
    protected boolean enabled;

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

    /**
     * Contact some payment provider to execute the payment. The call might either timeout, or the payment itself might
     * fail, or it is successful.
     */
    public void doPayment(String cardNumber, String cardOwner, String checksum, double total) throws PaymentFailedException {
        if(!enabled) {
            LOG.warn("Connecting to payment provider is disabled by configuration for testing purposes! " +
                "Returning as payment was successful.");
            return;
        }

        try {
            PaymentData paymentData = new PaymentData(cardNumber, cardOwner, checksum, total);
            Retry.decorateSupplier(retry, () -> template.postForObject(providerUrl, paymentData, Void.class)).get();
        } catch (RestClientException e) {
            LOG.error("Payment failed! Error: {}", e.getMessage());
            throw new PaymentFailedException("Payment failed", e);
        }
    }
}
