package org.example.burgerbanditten.user;

import org.example.burgerbanditten.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    // ── Opret konto ─────────────────────────────────────
    public User registerUser(User user) {

        if (user.getName() == null || user.getName().isEmpty()) {
            throw new IllegalArgumentException("Navn må ikke være tomt");
        }

        if (user.getMail() == null || user.getMail().isEmpty()) {
            throw new IllegalArgumentException("Email må ikke være tom");
        }

        if (!user.getMail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Email format er ugyldigt");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Adgangskode må ikke være tom");
        }

        if (userRepository.existsByMail(user.getMail())) {
            throw new IllegalArgumentException("Email er allerede registreret");
        }

        // Hash adgangskoden med BCrypt inden den gemmes
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(user);
        emailService.sendRegistrationConfirmation(savedUser.getMail(), savedUser.getName());

        return savedUser;
    }

    // ── Login ──────────────────────────────────────
    public User loginUser(String mail, String password) {

        if (mail == null || mail.isEmpty()) {
            throw new IllegalArgumentException("Email må ikke være tom");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Adgangskode må ikke være tom");
        }

        // Spring Security håndterer BCrypt sammenligning automatisk
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(mail, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (!authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Forkert email eller adgangskode");
        }

        return userRepository.findByMail(mail)
                .orElseThrow(() -> new IllegalArgumentException("Bruger ikke fundet"));
    }

    // ── Glemt adgangskode ──────────────────────────
    public void forgotPassword(String mail) {

        if (mail == null || mail.isEmpty()) {
            throw new IllegalArgumentException("Email må ikke være tom");
        }

        Optional<User> optionalUser = userRepository.findByMail(mail);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Ingen konto fundet med den email");
        }

        String resetToken = UUID.randomUUID().toString(); // Bruges som en midlertidig sikkerhedskode til nulstillelse
        String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;

        emailService.sendPasswordReset(mail, resetLink);
    }
}