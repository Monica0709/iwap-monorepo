package org.api.configs;

import org.api.filter.JwtAuthFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;


    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("lb://AUTH-SERVICE"))

                // Protected routes
                .route("designer-service", r -> r.path("/designer/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://DESIGNER-SERVICE"))

                .route("workflow-engine", r -> r.path("/engine/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://WORKFLOW-ENGINE"))

                .route("task-service", r -> r.path("/tasks/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://TASK-SERVICE"))

                .build();
    }
}
