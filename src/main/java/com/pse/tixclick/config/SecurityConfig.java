package com.pse.tixclick.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;  // Thêm EntryPoint vào

//    private final String[] PUBLIC_ENDPOINTS = {
//            "/account/**",
//            "/auth/**",
//            "/company-account/**",
//            "/event/**",
//            "event-image/**",
//            "/event-activity/**",
//            "/ticket/**",
//            "/company/**",
//            "/member/**",
//            "/swagger-ui/**",
//            "/v3/api-docs/**",
//            "/swagger-ui.html",
//            "/oauth2/**",
//            "/seat-map/**",
//            "/contract/**",
//            "/company-document/**",
//            "/company-verification/**",
//            "/member-activity/**",
//            "/contract-document/**",
//            "/background/**",
//    };
    private final String[] PUBLIC_ENDPOINTS = {
            "/account/**",
            "/auth/**",
            "/company-account/**",
            "/event/**",
            "event-image/**",
            "/event-activity/**",
            "/ticket/**",
            "/company/**",
            "/member/**",
            "/api/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/swagger-ui/index.html",
            "/oauth2/**",
            "/seat-map/**",
            "/contract/**",
            "/company-document/**",
            "/company-verification/**",
            "/member-activity/**",
            "/contract-document/**",
            "/background/**",
            "/ws/**",
            "/ticket-purchase/**",
            "/transaction/**",
            "/payment/**",
            "notification/**",
            "/api/tickets/**",
    };
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.PUT, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.DELETE, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            String referer = request.getHeader("Referer");
                            if (referer != null && referer.contains("/api/swagger-ui")) {
                                response.sendRedirect(referer);
                            } else {
                                response.sendRedirect("/auth/google/success");
                            }
                        })
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // Bắt lỗi Token hết hạn
                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        return new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:8081",
                "http://160.191.175.172:5173",
                "https://tixclick.site",
                "http://192.168.1.15:19006",
                "http://160.191.175.172:8080",
                "https://pay.payos.vn/"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
