package de.unistuttgart.t2.modulith.ui.config;

import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Improve TLD scanning at startup that is required for processing JSP files.
 */
@Configuration
public class ServletWebServerConfiguration {
    @Bean
    public ServletWebServerFactory embeddedServletContainerFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                StandardJarScanner jarScanner = (StandardJarScanner) context.getJarScanner();
                jarScanner.setScanManifest(false);
                jarScanner.setJarScanFilter((jarScanType, jarName) ->
                    jarName.contains("jstl") || jarName.contains("spring-web"));
            }
        };
    }
}
