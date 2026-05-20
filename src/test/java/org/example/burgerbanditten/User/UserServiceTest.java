package org.example.burgerbanditten.User;

import org.example.burgerbanditten.email.EmailService;
import org.example.burgerbanditten.user.Role;
import org.example.burgerbanditten.user.User;
import org.example.burgerbanditten.user.UserRepository;
import org.example.burgerbanditten.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User validUser;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(any())).thenReturn("hashedPassword");  // ← tilføj

        validUser = new User();
        validUser.setName("Anders Jensen");
        validUser.setMail("anders@gmail.com");
        validUser.setPassword("password123");
    }

    // ── Tomme felter ────────────────────────────────────

    @Test
    void skalKasteException_NårNavnErTomt() {
        validUser.setName("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser(validUser)
        );

        assertEquals("Navn må ikke være tomt", ex.getMessage());
    }

    @Test
    void skalKasteException_NårEmailErTom() {
        validUser.setMail("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser(validUser)
        );

        assertEquals("Email må ikke være tom", ex.getMessage());
    }

    @Test
    void skalKasteException_NårAdgangskodeErTom() {
        validUser.setPassword("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser(validUser)
        );

        assertEquals("Adgangskode må ikke være tom", ex.getMessage());
    }

    // ── Forkert email format ─────────────────────────────

    @Test
    void skalKasteException_NårEmailFormatErForkert() {
        validUser.setMail("detterikkeenemail");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser(validUser)
        );

        assertEquals("Email format er ugyldigt", ex.getMessage());
    }

    // ── Duplikeret email ─────────────────────────────────

    @Test
    void skalKasteException_NårEmailAlleredeErRegistreret() {
        when(userRepository.existsByMail("anders@gmail.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser(validUser)
        );

        assertEquals("Email er allerede registreret", ex.getMessage());
    }

    // ── Gyldig oprettelse ────────────────────────────────

    @Test
    void skalOpretteUser_NårAlleOplysningerErGyldige() {
        when(userRepository.existsByMail("anders@gmail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        User oprettetUser = userService.registerUser(validUser);

        // Tjek at brugeren er gemt
        verify(userRepository, times(1)).save(validUser);

        // Tjek at bekræftelsesmail er sendt
        verify(emailService, times(1))
                .sendRegistrationConfirmation(validUser.getMail(), validUser.getName());

        // Tjek at rolle er sat til CUSTOMER
        assertEquals(Role.CUSTOMER, oprettetUser.getRole());
    }
}


