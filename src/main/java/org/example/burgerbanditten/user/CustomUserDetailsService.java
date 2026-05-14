package org.example.burgerbanditten.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// Fortæller Spring Security hvordan den finder en bruger i databasen
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {

        // Find bruger i databasen ud fra email
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new UsernameNotFoundException("Bruger ikke fundet: " + mail));

        // Returnér Spring Security's UserDetails objekt med rolle
        return new org.springframework.security.core.userdetails.User(
                user.getMail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}