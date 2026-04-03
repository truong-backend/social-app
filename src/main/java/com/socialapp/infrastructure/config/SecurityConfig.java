package com.socialapp.infrastructure.config;

import com.socialapp.infrastructure.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://social-app-eight-eosin.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Swagger / OpenAPI ──────────────────────────────────────────────
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/api-docs",
                                "/api-docs/swagger-config"
                        ).permitAll()

                        // ── Auth (public) ──────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/confirm-email",
                                "/api/auth/login"
                        ).permitAll()

                        // prepare-reset và confirm-reset-code cần gửi email/code trước khi có token
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/prepare-reset-password",
                                "/api/auth/confirm-reset-code"
                        ).permitAll()

                        .requestMatchers(
                                "/ws/**",           // your WebSocket endpoint + SockJS paths
                                "/ws/info**",       // explicitly for SockJS info
                                "/sockjs/**"        // if needed
                        ).permitAll()

                        // update-password dùng SecurityUtil.currentAccountId() → cần JWT
                        // logout cũng cần JWT để blacklist token → cả 2 để authenticated
                        .requestMatchers(HttpMethod.PUT,  "/api/auth/update-password").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()

                        // ── Users ──────────────────────────────────────────────────────────
                        // GET profile công khai (xem profile người khác)
                        .requestMatchers(HttpMethod.GET, "/api/users/{targetId}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/search").authenticated()
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/users/me/name",
                                "/api/users/me/username",
                                "/api/users/me/birthdate",
                                "/api/users/me/bio",
                                "/api/users/me/profile-picture"
                        ).authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/messages/calls").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/messages/calls/*/answer").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/messages/calls/*/end").authenticated()


                        // ── Posts ──────────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/posts/{postId}").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/posts/{postId}/content").authenticated()
                        .requestMatchers(HttpMethod.PATCH,  "/api/posts/{postId}/privacy").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/{postId}").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/posts/{postId}/like").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/{postId}/like").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/posts/{postId}/share").authenticated()

                        // ── Comments ──────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/posts/{postId}/comments",
                                "/api/posts/{postId}/comments/{commentId}/replies"
                        ).authenticated()


                        .requestMatchers(HttpMethod.PUT,    "/api/comments/{commentId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/{commentId}").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/comments/{commentId}/like").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/{commentId}/like").authenticated()

                        // ── Messages ──────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/messages/calls/stringee-token").authenticated()

                        // ── Notifications ─────────────────────────────────────────────────
                        .requestMatchers("/api/notifications/**").authenticated()

                        // ── Relationships ─────────────────────────────────────────────────
                        .requestMatchers("/api/relationships/**").authenticated()

                        // ── Admin ─────────────────────────────────────────────────────────
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ── Fallback ──────────────────────────────────────────────────────
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}