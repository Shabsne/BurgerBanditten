package org.example.burgerbanditten.order.orderingwindow;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class OrderingWindowService {

    private final OrderingWindowRepository orderingWindowRepository;

    public OrderingWindowService(OrderingWindowRepository orderingWindowRepository) {
        this.orderingWindowRepository = orderingWindowRepository;
    }

    public OrderingWindow getOrderingWindow() {
        return orderingWindowRepository.findById(1L)
                .orElseGet(() -> orderingWindowRepository.save(
                        new OrderingWindow(LocalTime.of(16, 0), LocalTime.of(21, 0), true)
                ));
    }

    public OrderingWindow updateOrderingWindow(LocalTime openTime, LocalTime closeTime, boolean active) {
        OrderingWindow orderingWindow = getOrderingWindow();

        orderingWindow.setOpenTime(openTime);
        orderingWindow.setCloseTime(closeTime);
        orderingWindow.setActive(active);

        return orderingWindowRepository.save(orderingWindow);
    }

    public boolean isOrderingOpen() {
        OrderingWindow orderingWindow = getOrderingWindow();

        if (!orderingWindow.isActive()) {
            return false;
        }

        LocalTime now = LocalTime.now();

        return !now.isBefore(orderingWindow.getOpenTime())
                && !now.isAfter(orderingWindow.getCloseTime());
    }
}