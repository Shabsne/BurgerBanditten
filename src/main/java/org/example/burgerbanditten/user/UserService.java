package org.example.burgerbanditten.user;

import org.example.burgerbanditten.Email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public User registerUser(User user) {

        // Tjek at navn ikke er tomt
        if (user.getName() == null || user.getName().isEmpty()) {
            throw new IllegalArgumentException("Navn må ikke være tomt");
        }

        // Tjek at email ikke er tom
        if (user.getMail() == null || user.getMail().isEmpty()) {
            throw new IllegalArgumentException("Email må ikke være tom");
        }

        // Tjek at email format er gyldigt
        if (!user.getMail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Email format er ugyldigt");
        }

        // Tjek at adgangskode ikke er tom
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Adgangskode må ikke være tom");
        }

        // Afvis hvis email allerede er registreret
        if (userRepository.existsByMail(user.getMail())) {
            throw new IllegalArgumentException("Email er allerede registreret");
        }

        // Sæt rolle til CUSTOMER som standard
        user.setRole(Role.Customer);

        // Gem bruger i databasen
        User savedUser = userRepository.save(user);

        // Send bekræftelsesmail
        emailService.sendRegistrationConfirmation(savedUser.getMail(), savedUser.getName());

        return savedUser;
    }
}