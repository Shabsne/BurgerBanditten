package org.example.burgerbanditten.order.orderingtimewindow;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class OrderingTimeWindowService {

    private final OrderingTimeWindowRepository orderingTimeWindowRepository;

    public OrderingTimeWindowService(OrderingTimeWindowRepository orderingTimeWindowRepository) {
        this.orderingTimeWindowRepository = orderingTimeWindowRepository;
    }

    public OrderingTimeWindow getOrderingWindowForToday() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        return orderingTimeWindowRepository.findByDayOfWeek(today)
                .orElseGet(() -> orderingTimeWindowRepository.save(
                        new OrderingTimeWindow(today, LocalTime.of(12, 0), LocalTime.of(22, 0), true)
                ));
    }

    public OrderingTimeWindow updateOrderingWindow(
            DayOfWeek day,
            LocalTime openTime,
            LocalTime closeTime,
            boolean active
    ) {
        OrderingTimeWindow orderingTimeWindow = orderingTimeWindowRepository.findByDayOfWeek(day)
                        .orElseGet(() -> orderingTimeWindowRepository.save(createDefaultWindow(day)));

        orderingTimeWindow.setOpenTime(openTime);
        orderingTimeWindow.setCloseTime(closeTime);
        orderingTimeWindow.setActive(active);

        return orderingTimeWindowRepository.save(orderingTimeWindow);
    }

    public List<OrderingTimeWindow> getWeeklySchedule() {
        for (DayOfWeek day : DayOfWeek.values()) {
            orderingTimeWindowRepository.findByDayOfWeek(day)
                    .orElseGet(() -> orderingTimeWindowRepository.save(createDefaultWindow(day)));
        }
        return orderingTimeWindowRepository.findAll();
    }

    private OrderingTimeWindow createDefaultWindow(DayOfWeek day) {
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
            return new OrderingTimeWindow(day, LocalTime.of(12, 0), LocalTime.of(23, 0), true);
        }
        return new OrderingTimeWindow(day, LocalTime.of(12, 0), LocalTime.of(22, 0), true);
    }

    public boolean isOrderingOpen() {

        DayOfWeek today = LocalDate.now().getDayOfWeek();

        OrderingTimeWindow orderingTimeWindow =
                orderingTimeWindowRepository.findByDayOfWeek(today)
                        .orElse(null);

        if (orderingTimeWindow == null || !orderingTimeWindow.isActive()) {
            return false;
        }

        LocalTime now = LocalTime.now();

        return !now.isBefore(orderingTimeWindow.getOpenTime())
                && !now.isAfter(orderingTimeWindow.getCloseTime());
    }
}