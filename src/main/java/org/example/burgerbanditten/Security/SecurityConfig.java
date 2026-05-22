package org.example.burgerbanditten.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/favicon.ico",
                "/**/favicon.ico",
                "/css/**",
                "/js/**"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth

                        // ── Statiske HTML-sider ────────────────────────
                        .requestMatchers(
                                "/login.html", "/register.html",
                                "/menu.html", "/checkout.html", "/admin.html"
                        ).permitAll()

                        // ── Offentlige GET-endpoints ───────────────────
                        .requestMatchers(HttpMethod.GET,
                                "/menu", "/product/**", "/categories",
                                "/ingredients", "/opening-hours/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // ── Bruger-endpoints (login, register osv.) ────
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/logout",
                                "/api/users/forgot-password",
                                "/api/users/is-admin"
                        ).permitAll()

                        // ── Gæst checkout ──────────────────────────────
                        .requestMatchers("/api/orders/guest/checkout").permitAll()

                        // ── Bestillingsstatus – alle må se om det er åbent
                        .requestMatchers(HttpMethod.GET, "/api/orders/status").permitAll()

                        // ── Forudbestilling validering ─────────────────
                        .requestMatchers(HttpMethod.GET,  "/api/preorder/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/preorder/validate").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/preorder/next-available").permitAll()

                        // ── Admin-only: bestillings-switch ─────────────
                        .requestMatchers("/api/orders/admin/**").hasRole("ADMIN")

                        // ───────────── Hent en specifik ordre ─────────────
                        .requestMatchers(HttpMethod.GET, "/api/orders/*").hasRole("ADMIN")

                        // ── Admin-only: ordrer ─────────────────────────
                        .requestMatchers(
                                "/api/orders/pending",
                                "/api/orders/active",
                                "/api/orders/*/accept"
                        ).hasRole("ADMIN")

                        // ── Admin-only: produkter & åbningstider ───────
                        .requestMatchers("/api/products/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/opening-hours/**").hasRole("ADMIN")

                        // ── Logget-ind brugere: checkout ───────────────
                        .requestMatchers("/api/orders/checkout").hasAnyRole("CUSTOMER", "ADMIN")

                        // ── Alt andet kræver login ─────────────────────
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}