package de.unistuttgart.t2.modulith.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenAPIConfiguration {

    @Value("${T2_COMMON_VERSION:0.0.1}")
    private String version;

    @Bean
    public io.swagger.v3.oas.models.OpenAPI customOpenAPI() {
        return new io.swagger.v3.oas.models.OpenAPI().components(new Components()).info(new Info()
                .title("T2 Modulith API")
                .description("API of the T2-Project's modulith implementation.")
                .version(version));
    }
}
