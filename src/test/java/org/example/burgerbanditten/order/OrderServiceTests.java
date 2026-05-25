package org.example.burgerbanditten.order;

import org.example.burgerbanditten.email.EmailService;
import org.example.burgerbanditten.ingredient.IngredientRepository;
import org.example.burgerbanditten.preorder.PreOrderService;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.example.burgerbanditten.user.User;
import org.example.burgerbanditten.user.UserRepository;
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

    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private IngredientRepository ingredientRepository;
    private UserRepository userRepository;
    private OrderService orderService;
    private EmailService emailService;
    private PreOrderService preOrderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        userRepository = mock(UserRepository.class);
        productRepository = mock(ProductRepository.class);
        emailService     = mock(EmailService.class);
        preOrderService = mock(PreOrderService.class);
        orderService = new OrderService(
                orderRepository,
                userRepository,
                productRepository,
                emailService,
                preOrderService
                );
    }

    // ── Ikke-eksisterende ordre ──────────────────────────

    @Test
    void skalKasteException_NaarOrdreIkkeFindes() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.acceptOrder(99L)
        );

        assertEquals("Ordre ikke fundet: 99", ex.getMessage());
    }

    // ── #128 – Allerede accepteret ordre ─────────────────

    @Test
    void skalKasteException_NaarOrdreAlleredeErAccepteret() {
        Order order = buildOrder(OrderStatus.ACCEPTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                orderService.acceptOrder(1L)
        );

        assertTrue(ex.getMessage().contains("allerede accepteret"));
    }

    // ── Forkert status (fx COMPLETED) ────────────────────

    @Test
    void skalKasteException_NaarOrdreIkkeErPending() {
        Order order = buildOrder(OrderStatus.COMPLETED);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                orderService.acceptOrder(2L)
        );

        assertTrue(ex.getMessage().contains("COMPLETED"));
    }

    // ── #125 + #127 – Gyldig accept ──────────────────────

    @Test
    void skalAcceptereOrdre_NaarStatusErPending() {
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
    void skalReturnereTop5SalgsstatistikForPeriode() {
        Product burger = new Product();
        burger.setId(1L);
        burger.setName("Cheese Burger");

        Product cola = new Product();
        cola.setId(2L);
        cola.setName("Coca Cola");

        Product fries = new Product();
        fries.setId(3L);
        fries.setName("Fries");

        Product nuggets = new Product();
        nuggets.setId(4L);
        nuggets.setName("Nuggets");

        Product shake = new Product();
        shake.setId(5L);
        shake.setName("Shake");

        Product water = new Product();
        water.setId(6L);
        water.setName("Water");

        Product unused = new Product();
        unused.setId(7L);
        unused.setName("Unused");

        OrderItem burgerItem = new OrderItem();
        burgerItem.setProduct(burger);
        burgerItem.setQuantity(2);

        OrderItem colaItem = new OrderItem();
        colaItem.setProduct(cola);
        colaItem.setQuantity(7);

        OrderItem friesItem = new OrderItem();
        friesItem.setProduct(fries);
        friesItem.setQuantity(5);

        OrderItem nuggetsItem = new OrderItem();
        nuggetsItem.setProduct(nuggets);
        nuggetsItem.setQuantity(4);

        OrderItem shakeItem = new OrderItem();
        shakeItem.setProduct(shake);
        shakeItem.setQuantity(3);

        OrderItem waterItem = new OrderItem();
        waterItem.setProduct(water);
        waterItem.setQuantity(1);

        Order order = new Order();
        order.setPrice(183.0);
        order.setOrderItems(List.of(burgerItem, colaItem, friesItem, nuggetsItem, shakeItem, waterItem));

        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 18);

        when(productRepository.findAll()).thenReturn(List.of(burger, cola, fries, nuggets, shake, water, unused));
        when(orderRepository.findSalesOrders(
                List.of(OrderStatus.ACCEPTED, OrderStatus.COMPLETED),
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 19, 0, 0)
        )).thenReturn(List.of(order));

        var result = orderService.getSalesStatistics(from, to);

        assertEquals(183.0, result.totalRevenue());
        assertEquals(5, result.productSales().size());
        assertEquals("Coca Cola", result.productSales().get(0).productName());
        assertEquals(7, result.productSales().get(0).quantitySold());
        assertEquals("Fries", result.productSales().get(1).productName());
        assertEquals("Nuggets", result.productSales().get(2).productName());
        assertEquals("Shake", result.productSales().get(3).productName());
        assertEquals("Cheese Burger", result.productSales().get(4).productName());
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
