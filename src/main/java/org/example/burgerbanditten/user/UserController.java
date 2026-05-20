package org.example.burgerbanditten.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // POST register – Opret konto
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User createdUser = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // POST login – Login
    // FIX: Spring Security 6 kræver at SecurityContext gemmes eksplicit til HTTP-session.
    // Uden dette ser hvert efterfølgende request ud som anonymt → 403 på alle beskyttede endpoints.
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> loginData,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String mail     = loginData.get("mail");
            String password = loginData.get("password");

            User loggedInUser = userService.loginUser(mail, password);

            // Gem SecurityContext eksplicit i HTTP-sessionen.
            // I Spring Security 6 sker dette IKKE automatisk via REST endpoints.
            SecurityContext context = SecurityContextHolder.getContext();
            new HttpSessionSecurityContextRepository().saveContext(context, request, response);

            return ResponseEntity.ok(loggedInUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // POST forgot-password – Glemt adgangskode
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String mail = body.get("mail");
            userService.forgotPassword(mail);
            return ResponseEntity.ok("Nulstillingslink er sendt til din email");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // POST logout – Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("/menu.html");
    }

    // GET is-admin – Tjekker om den nuværende session tilhører en admin
    @GetMapping("/is-admin")
    public ResponseEntity<Boolean> isAdmin(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            return ResponseEntity.ok(false);
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ADMIN"));
        return ResponseEntity.ok(isAdmin);
    }
}