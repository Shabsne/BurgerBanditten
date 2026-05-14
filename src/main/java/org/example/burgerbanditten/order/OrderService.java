package org.example.burgerbanditten.order;

import org.example.burgerbanditten.Email.EmailService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    // #125 – Skift ordrestatus til ACCEPTED
    // #126 – Ordre er herefter tilgængelig som "aktiv ordre" via getActiveOrders()
    // #128 – Kast exception hvis ordren allerede er accepteret
    public Order acceptOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre ikke fundet: " + orderId));

        if (order.getOrderStatus() == OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Ordre #" + orderId + " er allerede accepteret");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Kun ventende ordrer kan accepteres. Nuværende status: " + order.getOrderStatus()
            );
        }

        order.setOrderStatus(OrderStatus.ACCEPTED);
        Order savedOrder = orderRepository.save(order);

        // #124 (email-del) – Send besked til kunden om at ordren er accepteret
        String customerEmail = order.getUser().getMail();
        String customerName  = order.getUser().getName();
        emailService.sendOrderAcceptedNotification(customerEmail, customerName, order.getId());

        return savedOrder;
    }

    // Frontend: tab "Ventende"
    public List<Order> getPendingOrders() {
        return orderRepository.findByOrderStatus(OrderStatus.PENDING);
    }

    // #126 – Frontend: tab "Aktive"
    public List<Order> getActiveOrders() {
        return orderRepository.findByOrderStatus(OrderStatus.ACCEPTED);
    }
}