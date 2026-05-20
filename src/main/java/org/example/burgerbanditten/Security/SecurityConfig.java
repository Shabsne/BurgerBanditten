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
                "/h2-console/**",
                "/css/**",
                "/js/**" // Giver adgang til alt i JS-mappen (husk at Cart.js nu skal ligge der)
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        // Offentlige HTML-sider (admin.html tilføjet her, dataen beskyttes via API endpoints)
                        .requestMatchers(
                                "/login.html",
                                "/register.html",
                                "/menu.html",
                                "/checkout.html",
                                "/admin.html"
                        ).permitAll()

                        // Offentlige GET endpoints til at hente mad og åbningstider
                        .requestMatchers(HttpMethod.GET, "/menu", "/product/**", "/categories", "/ingredients", "/opening-hours/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // Offentlige bruger endpoints
                        .requestMatchers("/api/users/register", "/api/users/login", "/api/users/forgot-password").permitAll()
                        .requestMatchers("/api/users/is-admin").permitAll()

                        // Åben helt op for gæste-checkout
                        .requestMatchers("/api/orders/guest/checkout").permitAll()

                        // Restriktioner for registrerede brugere og admins
                        .requestMatchers("/api/orders/checkout").hasAnyAuthority("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/orders/pending", "/api/orders/active", "/api/orders/*/accept").hasAuthority("ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // Alt andet kræver login
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}