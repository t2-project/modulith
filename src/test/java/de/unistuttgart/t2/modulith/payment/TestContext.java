package de.unistuttgart.t2.modulith.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class TestContext {

    public static final int timeout = 5;

    public static final String testUrl = "http://foo.bar/pay";

    @Bean
    public RestTemplate template() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        return restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(timeout))
            .setReadTimeout(Duration.ofSeconds(timeout)).build();
    }

    @Bean
    public PaymentService service(@Autowired RestTemplate restTemplate) {
        return new PaymentService(testUrl, restTemplate);
    }
}
