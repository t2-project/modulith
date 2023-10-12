package de.unistuttgart.t2.modulith.common.scaling;

import de.unistuttgart.t2.modulith.common.scaling.memory.MemoryLeaker;
import de.unistuttgart.t2.modulith.common.scaling.request.RequestDenier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan
@EnableWebMvc
public class AutoscalingMiddleware implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestDenier()).excludePathPatterns("/autoscaling/**");
        registry.addInterceptor(new MemoryLeaker()).excludePathPatterns("/autoscaling/**");
    }
}
