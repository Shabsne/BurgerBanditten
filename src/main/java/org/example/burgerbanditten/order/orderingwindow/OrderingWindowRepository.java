package org.example.burgerbanditten.order.orderingwindow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface OrderingWindowRepository extends JpaRepository<OrderingWindow, Long> {

    Optional<OrderingWindow> findByDayOfWeek(DayOfWeek dayOfWeek);
}
