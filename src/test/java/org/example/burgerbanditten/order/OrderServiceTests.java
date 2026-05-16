package org.example.burgerbanditten.order;

import org.example.burgerbanditten.email.EmailService;
import org.example.burgerbanditten.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTests {

    private OrderService orderService;
    private OrderRepository orderRepository;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        emailService     = mock(EmailService.class);
        orderService     = new OrderService(orderRepository, emailService);
    }

    // ── Ikke-eksisterende ordre ──────────────────────────

    @Test
    void skalKasteException_NårOrdreIkkeFindes() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.acceptOrder(99L)
        );

        assertEquals("Ordre ikke fundet: 99", ex.getMessage());
    }

    // ── #128 – Allerede accepteret ordre ─────────────────

    @Test
    void skalKasteException_NårOrdreAlleredeErAccepteret() {
        Order order = buildOrder(OrderStatus.ACCEPTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                orderService.acceptOrder(1L)
        );

        assertTrue(ex.getMessage().contains("allerede accepteret"));
    }

    // ── Forkert status (fx COMPLETED) ────────────────────

    @Test
    void skalKasteException_NårOrdreIkkeErPending() {
        Order order = buildOrder(OrderStatus.COMPLETED);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                orderService.acceptOrder(2L)
        );

        assertTrue(ex.getMessage().contains("COMPLETED"));
    }

    // ── #125 + #127 – Gyldig accept ──────────────────────

    @Test
    void skalAcceptereOrdre_NårStatusErPending() {
        Order order = buildOrder(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.acceptOrder(1L);

        // Ordre gemt med ny status
        verify(orderRepository, times(1)).save(order);
        assertEquals(OrderStatus.ACCEPTED, result.getOrderStatus());

        // Kunde notificeret (#124)
        verify(emailService, times(1))
                .sendOrderAcceptedNotification(
                        order.getUser().getMail(),
                        order.getUser().getName(),
                        order.getId()
                );
    }

    // ── Hjælper ──────────────────────────────────────────

    private Order buildOrder(OrderStatus status) {
        User user = new User();
        user.setName("Test Kunde");
        user.setMail("kunde@test.dk");

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setOrderStatus(status);
        return order;
    }
}