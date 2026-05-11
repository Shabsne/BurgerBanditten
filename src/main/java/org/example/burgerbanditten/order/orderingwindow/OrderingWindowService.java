package org.example.burgerbanditten.order.orderingwindow;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class OrderingWindowService {

    private final OrderingWindowRepository orderingWindowRepository;

    public OrderingWindowService(OrderingWindowRepository orderingWindowRepository) {
        this.orderingWindowRepository = orderingWindowRepository;
    }

    public OrderingWindow getOrderingWindowForToday() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        return orderingWindowRepository.findByDayOfWeek(today)
                .orElseGet(() -> orderingWindowRepository.save(
                        new OrderingWindow(today, LocalTime.of(12, 0), LocalTime.of(22, 0), true)
                ));
    }

    public OrderingWindow updateOrderingWindow(
            DayOfWeek day,
            LocalTime openTime,
            LocalTime closeTime,
            boolean active
    ) {
        OrderingWindow orderingWindow = orderingWindowRepository.findByDayOfWeek(day)
                        .orElseGet(() -> orderingWindowRepository.save(createDefaultWindow(day)));

        orderingWindow.setOpenTime(openTime);
        orderingWindow.setCloseTime(closeTime);
        orderingWindow.setActive(active);

        return orderingWindowRepository.save(orderingWindow);
    }

    public List<OrderingWindow> getWeeklySchedule() {
        for (DayOfWeek day : DayOfWeek.values()) {
            orderingWindowRepository.findByDayOfWeek(day)
                    .orElseGet(() -> orderingWindowRepository.save(createDefaultWindow(day)));
        }
        return orderingWindowRepository.findAll();
    }

    private OrderingWindow createDefaultWindow(DayOfWeek day) {
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
            return new OrderingWindow(day, LocalTime.of(12, 0), LocalTime.of(23, 0), true);
        }
        return new OrderingWindow(day, LocalTime.of(12, 0), LocalTime.of(22, 0), true);
    }

    public boolean isOrderingOpen() {

        DayOfWeek today = LocalDate.now().getDayOfWeek();

        OrderingWindow orderingWindow =
                orderingWindowRepository.findByDayOfWeek(today)
                        .orElse(null);

        if (orderingWindow == null || !orderingWindow.isActive()) {
            return false;
        }

        LocalTime now = LocalTime.now();

        return !now.isBefore(orderingWindow.getOpenTime())
                && !now.isAfter(orderingWindow.getCloseTime());
    }
}