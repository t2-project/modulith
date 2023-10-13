package de.unistuttgart.t2.modulith.uibackend;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Interacts with other modules to prepare data for the actual UI.
 *
 * @author maumau
 */
@Service
public class UIBackendModule {

    @Value("${T2_COMMON_VERSION:0.0.1}")
    private String version;

    @Bean
    public RestTemplate template() {
        return new RestTemplate();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().components(new Components()).info(new Info()
                .title("UIBackend service API")
                .description("API of the T2-Project's UIbackend service.")
                .version(version));
    }
}
