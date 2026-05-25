package org.example.burgerbanditten.User;

import org.example.burgerbanditten.user.User;
import org.example.burgerbanditten.user.UserRepository;
import org.example.burgerbanditten.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.example.burgerbanditten.email.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
 class UserIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Test Bruger");
        testUser.setMail("test@example.com");
        testUser.setPassword("Hemmeligt123");
    }

    // ── registerUser ─────────────────────────────────────────────────────────

    @Test
    void registerUser_returnererGemtBruger_medHashetAdgangskode() {
        when(userRepository.existsByMail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$hashed");
        when(userRepository.save(any())).thenReturn(testUser);

        User result = userService.registerUser(testUser);

        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("Hemmeligt123");
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void registerUser_kasterException_vedDuplikatMail() {
        when(userRepository.existsByMail("test@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(testUser));

        assertEquals("Email er allerede registreret", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_kasterException_vedUgyldigtEmailFormat() {
        testUser.setMail("detterikkeenemail");

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(testUser));

        verify(userRepository, never()).save(any());
    }

    // ── loginUser ────────────────────────────────────────────────────────────

    @Test
    void loginUser_returnererBruger_vedKorrekteCredentials() {
        // 3-argument constructor sætter isAuthenticated() = true
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken("test@example.com", "Hemmeligt123", List.of());
        when(authenticationManager.authenticate(any())).thenReturn(token);
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.loginUser("test@example.com", "Hemmeligt123");

        assertNotNull(result);
        assertEquals("test@example.com", result.getMail());
    }

    @Test
    void loginUser_kasterException_vedForkertAdgangskode() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Forkert adgangskode"));

        assertThrows(BadCredentialsException.class,
                () -> userService.loginUser("test@example.com", "ForkertPassword"));
    }

    // ── findByMail ───────────────────────────────────────────────────────────

    @Test
    void findByMail_returnereBruger_naarDenFindes() {
        when(userRepository.findByMail("test@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.findByMail("test@example.com");

        assertEquals("test@example.com", result.getMail());
    }

    @Test
    void findByMail_kasterException_vedUkendtMail() {
        when(userRepository.findByMail("ingen@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.findByMail("ingen@example.com"));

        assertTrue(ex.getMessage().contains("ikke fundet"));
    }
}
