package de.unistuttgart.t2.modulith.uibackend.supplicants;

import de.unistuttgart.t2.modulith.uibackend.UIBackendService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("test")
public class TestContext {

    @Bean
    public RestTemplate template() {
        return new RestTemplate();
    }

    @Bean
    public UIBackendService service() {
        return new UIBackendService();
    }
}
