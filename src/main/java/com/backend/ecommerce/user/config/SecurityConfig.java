package com.backend.ecommerce.user.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true) // Enables @Secured annotation
public class SecurityConfig {

    private final JWTRequestFilter jwtRequestFilter;

    public SecurityConfig(JWTRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )


// In SecurityConfig.java, update the authorizeHttpRequests section:

                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/users/register/**",
                                "/api/users/verify/**",
                                "/api/users/login",
                                "/api/users/logout",
                                "/api/users/forgot/**",
                                "/api/users/reset/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Guest cart endpoints (public)
                        .requestMatchers("/api/guest/cart/**").permitAll()

                        // Public order tracking (no authentication required)
                        .requestMatchers("/api/orders/track/**").permitAll()

                        // Public shop endpoints
                        .requestMatchers(
                                "/api/shops",
                                "/api/shops/featured",
                                "/api/shops/search",
                                "/api/shops/slug/**",
                                "/api/shops/*"
                        ).permitAll()

                        // Public product endpoints
                        .requestMatchers(
                                "/api/products",
                                "/api/products/filter",
                                "/api/products/search",
                                "/api/products/featured",
                                "/api/products/on-sale",
                                "/api/products/category/**",
                                "/api/products/shop/**",
                                "/api/products/slug/**",
                                "/api/products/*"
                        ).permitAll()

                        // Public category endpoints
                        .requestMatchers(
                                "/api/categories",
                                "/api/categories/tree",
                                "/api/categories/featured",
                                "/api/categories/menu",
                                "/api/categories/root",
                                "/api/categories/*/subcategories",
                                "/api/categories/*/breadcrumb",
                                "/api/categories/slug/**",
                                "/api/categories/*"
                        ).permitAll()

                        // Seller registration (authenticated users only)
                        .requestMatchers("/api/seller/register", "/api/seller/profile", "/api/seller/status").authenticated()

                        // Seller shop endpoints (verified sellers only)
                        .requestMatchers("/api/seller/shop/**").authenticated()

                        // Seller product endpoints (verified sellers only)
                        .requestMatchers("/api/seller/products/**").authenticated()

                        // Seller order endpoints (verified sellers only)
                        .requestMatchers("/api/seller/orders/**").authenticated()

                        // Favorites endpoints (authenticated users only)
                        .requestMatchers("/api/favorites/**").authenticated()

                        // Cart endpoints (authenticated users only)
                        .requestMatchers("/api/cart/**").authenticated()

                        // Order endpoints (authenticated users only except tracking)
                        .requestMatchers("/api/orders/**").authenticated()

                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // Authenticated endpoints
                        .anyRequest().authenticated()
                )




                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint((request, response, authException) -> {
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                                })
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                                })
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:5173")); // Update with your frontend URL
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
