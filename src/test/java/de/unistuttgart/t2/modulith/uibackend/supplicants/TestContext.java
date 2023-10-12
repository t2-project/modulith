package de.unistuttgart.t2.modulith.uibackend.supplicants;

import de.unistuttgart.t2.modulith.uibackend.UIBackendService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestContext {

    @Bean
    public UIBackendService service() {
        return new UIBackendService();
    }
}
