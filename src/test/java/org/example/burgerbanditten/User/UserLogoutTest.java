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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLogoutTest {

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

    // Test: Log ind, brug systemet, log ud
    // og tjek at session er ryddet
    @Test
    void skalRyddeSecurityContext_NårBrugerLoggerUd() {

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "anders@gmail.com", "password123"
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Bekræft at brugeren er logget ind
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        // Log ud – ryd security context
        SecurityContextHolder.clearContext();

        // Bekræft at session er ryddet efter logout
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void skalIkkeHaveAdgang_EfterLogout() {

        // Log ind
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "anders@gmail.com", "password123"
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Log ud
        SecurityContextHolder.clearContext();

        // Bekræft at der ikke er nogen authentication
        Authentication efterLogout = SecurityContextHolder.getContext().getAuthentication();
        assertNull(efterLogout);
    }

    @Test
    void skalKunneLoggeIndIgen_EfterLogout() {

        // Log ind
        Authentication authFoer = new UsernamePasswordAuthenticationToken(
                "anders@gmail.com", "password123"
        );
        SecurityContextHolder.getContext().setAuthentication(authFoer);

        // Log ud
        SecurityContextHolder.clearContext();

        // Log ind igen
        Authentication authEfter = mock(Authentication.class);
        when(authEfter.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any()))
                .thenReturn(authEfter);
        when(userRepository.findByMail("anders@gmail.com"))
                .thenReturn(Optional.of(eksisterendeBruger));

        User bruger = userService.loginUser("anders@gmail.com", "password123");

        assertNotNull(bruger);
        assertEquals("anders@gmail.com", bruger.getMail());
    }
}