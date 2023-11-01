package de.unistuttgart.t2.modulith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ModulithApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ModulithApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(de.unistuttgart.t2.modulith.ModulithApplication.class);
	}
}