package com.stu.socialnetworkapi.config;

import com.stu.socialnetworkapi.enums.AccountRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        @Value("${origin.front-end}")
        private String frontEndOrigin;

        private final CustomJwtDecoder jwtDecoder;
        private final CustomOAuth2UserService oauth2UserService;
        private final OAuth2SuccessHandler oauth2SuccessHandler;

        private static final String[] PUBLIC_ENDPOINTS = {
                "/v1/auth/**",
                "/v1/register/**",
                "/actuator/**",
                "/v1/forgot-password/**",
                "/v1/update-password/**",
                "/v1/users/{username}",
                "/v1/files/{id}",
                "/v1/notifications/send",
                "/v1/posts/newsfeed",
                "/v1/posts/{id}",
                "/v1/posts/of-user/{username}",
                "/v1/comments/of-post/{postId}",
                "/v1/stringee/**",
                "/v1/test/**",
                "/ws/**",
                "/oauth2/**",           // Google OAuth2 redirect endpoint
                "/login/oauth2/**"      // Google OAuth2 callback từ Google
        };

        private static final String[] ADMIN_ENDPOINTS = {
                "/v1/statistics",
                "/v1/users/admin/**"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                        .csrf(AbstractHttpConfigurer::disable)
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        // OAuth2 Authorization Code Flow cần session để lưu state/nonce
                        // Chỉ REST API mới STATELESS, OAuth2 endpoints cần IF_REQUIRED
                        .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        )
                        .authorizeHttpRequests(request -> request
                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                .requestMatchers(ADMIN_ENDPOINTS).hasAuthority(AccountRole.ADMIN.name())
                                .anyRequest().authenticated()
                        )
                        // JWT Resource Server — cho các REST API bình thường
                        .oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwtConfigurer -> jwtConfigurer
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                                        .decoder(jwtDecoder))
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                        )
                        // OAuth2 Login — cho luồng Google redirect
                        .oauth2Login(oauth2 -> oauth2
                                .userInfoEndpoint(ep -> ep.userService(oauth2UserService))
                                .successHandler(oauth2SuccessHandler)
                        )
                        .build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration corsConfiguration = new CorsConfiguration();
                corsConfiguration.setAllowedOriginPatterns(List.of(
                        frontEndOrigin,
                        "https://*.stringee.com",
                        "http://localhost:3000",
                        "http://localhost:5173"
                ));
                corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                corsConfiguration.setAllowedHeaders(List.of("*"));
                corsConfiguration.setAllowCredentials(true);
                corsConfiguration.setExposedHeaders(List.of("Set-Cookie"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", corsConfiguration);
                return source;
        }

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public CorsFilter corsFilter() {
                return new CorsFilter(corsConfigurationSource());
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthorityPrefix("");
                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
                return jwtAuthenticationConverter;
        }
}