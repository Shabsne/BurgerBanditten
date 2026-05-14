package org.example.burgerbanditten.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Bruges til at tjekke om email allerede er registreret
    boolean existsByMail(String mail);

    // Bruges til login
    Optional<User> findByMail(String mail);
}
