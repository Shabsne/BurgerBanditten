package org.example.burgerbanditten.User;

import org.example.burgerbanditten.Email.EmailService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication; // RETTET IMPORT

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    private User eksisterendeBruger;

    @BeforeEach
    void setUp() {
        eksisterendeBruger = new User();
        eksisterendeBruger.setName("Anders Jensen");
        eksisterendeBruger.setMail("anders@gmail.com");
        eksisterendeBruger.setPassword("password123");
        eksisterendeBruger.setRole(Role.CUSTOMER);
    }

    // ── Login tests ──────────────────────────────────────

    @Test
    void skalLoggeInd_NårOplysningerErKorrekte() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByMail("anders@gmail.com"))
                .thenReturn(Optional.of(eksisterendeBruger));

        User loggetIndBruger = userService.loginUser("anders@gmail.com", "password123");

        assertNotNull(loggetIndBruger);
        assertEquals("anders@gmail.com", loggetIndBruger.getMail());
    }

    @Test
    void skalKasteException_NårEmailIkkeFindes() {
        // Vi simulerer at AuthManager kaster en fejl, da mailen ikke findes
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new IllegalArgumentException("Forkert email eller adgangskode"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.loginUser("forkert@gmail.com", "password123")
        );

        assertEquals("Forkert email eller adgangskode", ex.getMessage());
    }

    @Test
    void skalKasteException_NårAdgangskodeErForkert() {
        // Vi simulerer at AuthManager kaster en fejl pga. forkert password
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new IllegalArgumentException("Forkert email eller adgangskode"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.loginUser("anders@gmail.com", "forkertPassword")
        );

        assertEquals("Forkert email eller adgangskode", ex.getMessage());
    }

    @Test
    void skalKasteException_NårEmailErTomVedLogin() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.loginUser("", "password123")
        );

        assertEquals("Email må ikke være tom", ex.getMessage());
    }

    @Test
    void skalKasteException_NårAdgangskodeErTomVedLogin() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.loginUser("anders@gmail.com", "")
        );

        assertEquals("Adgangskode må ikke være tom", ex.getMessage());
    }

    // ── Glemt adgangskode tests ──────────────────────────

    @Test
    void skalSendeNulstillingsmail_NårEmailFindes() {
        when(userRepository.findByMail("anders@gmail.com"))
                .thenReturn(Optional.of(eksisterendeBruger));

        userService.forgotPassword("anders@gmail.com");

        verify(emailService, times(1))
                .sendPasswordReset(eq("anders@gmail.com"), anyString());
    }

    @Test
    void skalKasteException_NårEmailIkkeFindesVedNulstilling() {
        when(userRepository.findByMail("ukendt@gmail.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.forgotPassword("ukendt@gmail.com")
        );

        assertEquals("Ingen konto fundet med den email", ex.getMessage());
    }
}