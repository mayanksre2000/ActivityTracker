package com.fitness.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity   //Enables Reactive Security for a WebFlux app (non-blocking).
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {  //Defines the security rules for HTTP requests
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)   //Disables CSRF (Cross-Site Request Forgery) — usually for frontend forms, so we skip it here.
                .authorizeExchange(exchange -> exchange //Defines which requests are allowed without authentication, and which require authentication.

//                        .pathMatchers("/actuator/*").permitAll()
                                .anyExchange().authenticated() //This says: Every request must be authenticated. If no valid token → ❌.
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())) //This turns your gateway into a Resource Server that expects JWT tokens in Authorization header.
                .build();
    }
        //    Under the hood, Spring will:
        //
        //    Use a JWKS URI (JWK Set URI available in yml file) to fetch the public keys from Keycloak
        //
        //    Use those keys to verify the JWT signature. spring does it automatically
        //
        //    Parse the token and load its claims (like email, user ID, roles)
        //
        //    Make those claims available in Spring's SecurityContext


    //    ❓ Wait, what is a JWT? How does login work?
                    //User logs in (e.g., on frontend) via Keycloak.
                    //
                    //Keycloak gives a JWT token.
                    //
                    //Frontend sends this token in every API request:
                    //
                        //Authorization: Bearer <JWT>
                    //Gateway verifies this token using public keys from Keycloak.
                    //
                    //If valid → user is authenticated, request continues

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:5173"));
//        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-User-ID"));
//        config.setAllowCredentials(true);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/api/**", config);
//        return source;
//    }
}