package de.unistuttgart.t2.modulith.ui;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * WebUi for the human user. It talks to the UI Backend.
 *
 * @author maumau
 */
//@Configuration
public class WebUIComponent extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(WebUIComponent.class);
    }
}
