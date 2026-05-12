package org.example.burgerbanditten.order.orderingtimewindow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface OrderingTimeWindowRepository extends JpaRepository<OrderingTimeWindow, Long> {

    Optional<OrderingTimeWindow> findByDayOfWeek(DayOfWeek dayOfWeek);
}
