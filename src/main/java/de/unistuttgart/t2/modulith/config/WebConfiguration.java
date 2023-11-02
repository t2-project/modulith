package de.unistuttgart.t2.modulith.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Method `setUseTrailingSlashMatch` is deprecated.
        // See https://github.com/spring-projects/spring-framework/issues/28552
        // TODO Check UI to not use URLs with a trailing slash
        configurer.setUseTrailingSlashMatch(true);
    }
}
