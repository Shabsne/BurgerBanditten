package org.example.burgerbanditten.order.openinghours;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface OpeningHoursRepository extends JpaRepository<OpeningHours, Long> {

    Optional<OpeningHours> findByDayOfWeek(DayOfWeek dayOfWeek);
}
