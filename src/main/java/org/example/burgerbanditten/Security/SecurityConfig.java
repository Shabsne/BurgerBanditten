package org.example.burgerbanditten.Security;


import org.example.burgerbanditten.user.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
    public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService customUserDetailsService;

        // BCrypt – krypterer adgangskoder
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        // AuthenticationManager – bruges til login
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
            return config.getAuthenticationManager();
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
            return web -> web.ignoring().requestMatchers("/h2-console/**");
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                    .authorizeHttpRequests(auth -> auth

                            // Alle kan se disse sider
                            .requestMatchers(
                                    "/login.html",
                                    "/register.html",
                                    "/menu.html",
                                    "/css/**",
                                    "/js/**"
                            ).permitAll()

                            .requestMatchers("/api/users/is-admin").permitAll()

                            .requestMatchers(HttpMethod.GET, "/api/ingredients/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/ingredients/**").hasRole("ADMIN")

                            .requestMatchers(HttpMethod.GET,
                                    "/menu",
                                    "/product/**",
                                    "/categories",
                                    "/ingredients")
                            .permitAll()

                            .requestMatchers(HttpMethod.GET, "/h2-console/**").permitAll()


                            // Alle kan registrere og logge ind
                            .requestMatchers(
                                    "/api/users/register",
                                    "/api/users/login",
                                    "/api/users/forgot-password"
                            ).permitAll()

                            // Alle kan se menuen og bestille som gæst
                            .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                            .requestMatchers("/api/orders/guest/**").permitAll()

                            // Kun ADMIN må tilgå admin endpoints
                            .requestMatchers("/admin/**").hasRole("ADMIN")

                            // CUSTOMER og ADMIN kan bestille
                            .requestMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN")

                            // Alt andet kræver login
                            .anyRequest().authenticated()
                    );

            return http.build();
        }
    }

