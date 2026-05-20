package org.example.burgerbanditten.order;

import org.example.burgerbanditten.email.EmailService;
import org.example.burgerbanditten.preorder.PreOrderService;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.example.burgerbanditten.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTests {

    private OrderService orderService;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private EmailService emailService;
    private PreOrderService preOrderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        emailService     = mock(EmailService.class);
        preOrderService = mock(PreOrderService.class);
        orderService     = new OrderService(orderRepository, productRepository, emailService, preOrderService);
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

    // ISSUE #111
    @Test
    void skalReturnereSalgsstatistikForPeriode() {
        Product burger = new Product();
        burger.setId(1L);
        burger.setName("Cheese Burger");

        Product cola = new Product();
        cola.setId(2L);
        cola.setName("Coca Cola");

        OrderItem burgerItem = new OrderItem();
        burgerItem.setProduct(burger);
        burgerItem.setQuantity(2);

        Order order = new Order();
        order.setPrice(183.0);
        order.setOrderItems(List.of(burgerItem));

        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 18);

        when(productRepository.findAll()).thenReturn(List.of(burger, cola));
        when(orderRepository.findSalesOrders(
                List.of(OrderStatus.ACCEPTED, OrderStatus.COMPLETED),
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 19, 0, 0)
        )).thenReturn(List.of(order));

        var result = orderService.getSalesStatistics(from, to);

        assertEquals(183.0, result.totalRevenue());
        assertEquals(2, result.productSales().get(0).quantitySold());
        assertEquals(0, result.productSales().get(1).quantitySold());
    }

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
