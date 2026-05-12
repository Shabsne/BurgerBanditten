package org.example.burgerbanditten.order.openinghours;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface HolidayOpeningHoursRepository extends JpaRepository<HolidayOpeningHours, Long> {
    Optional<HolidayOpeningHours> findByDate(LocalDate date);
}
