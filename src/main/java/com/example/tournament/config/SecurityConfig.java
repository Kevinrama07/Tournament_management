package com.example.tournament.config;

import com.example.tournament.security.AppUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    public static final String API = "/api/**";

    private final AppUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public SecurityConfig(AppUserDetailsService userDetailsService, ObjectMapper objectMapper) {
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Chaîne dédiée à l'API REST : authentification HTTP Basic, sans session,
     * réponses JSON en cas d'erreur d'authentification.
     */
    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(API)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/**")
                                .hasAnyRole("ADMIN", "MANAGER", "OPERATOR", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/tournaments")
                                .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/tournaments/**")
                                .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/tournaments/*/status")
                                .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/tournaments/**")
                                .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/tournaments/*/teams")
                                .hasAnyRole("ADMIN", "MANAGER", "OPERATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/tournaments/*/teams/*")
                                .hasAnyRole("ADMIN", "MANAGER", "OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/api/tournaments/*/generate-matches")
                                .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/tournaments/*/start")
                                .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/matches/*/result")
                                .hasAnyRole("ADMIN", "MANAGER", "OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/api/teams")
                                .hasAnyRole("ADMIN", "MANAGER", "OPERATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/teams/*")
                                .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/teams/*")
                                .hasAnyRole("ADMIN")
                        .requestMatchers("/api/teams/*/players/**")
                                .hasAnyRole("ADMIN", "MANAGER", "OPERATOR")
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint(
                        (request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            Map<String, Object> body = new LinkedHashMap<>();
                            body.put("timestamp", LocalDateTime.now().toString());
                            body.put("status", 401);
                            body.put("error", "Unauthorized");
                            body.put("message", "Authentification requise : identifiants manquants ou invalides.");
                            body.put("path", request.getRequestURI());
                            objectMapper.writeValue(response.getOutputStream(), body);
                        }))
                .exceptionHandling(e -> e.accessDeniedHandler(
                        (request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            Map<String, Object> body = new LinkedHashMap<>();
                            body.put("timestamp", LocalDateTime.now().toString());
                            body.put("status", 403);
                            body.put("error", "Forbidden");
                            body.put("message", "Accès refusé : droits insuffisants.");
                            body.put("path", request.getRequestURI());
                            objectMapper.writeValue(response.getOutputStream(), body);
                        }));
        return http.build();
    }

    /**
     * Chaîne de l'application web (Thymeleaf) : formulaire de connexion, sessions.
     */
    @Bean
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/webjars/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}