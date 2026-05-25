package org.example.burgerbanditten.order;

import org.example.burgerbanditten.email.EmailService;
import org.example.burgerbanditten.order.dto.*;
import org.example.burgerbanditten.preorder.PreOrderService;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.example.burgerbanditten.user.User;
import org.example.burgerbanditten.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final PreOrderService preOrderService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        EmailService emailService,
                        PreOrderService preOrderService,
                        ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.emailService    = emailService;
        this.preOrderService = preOrderService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ── Gæstebestilling ──────────────────────────────────────────────────────
    public Order createGuestOrder(GuestOrderRequest request) {
        Order order = new Order();
        order.setOrderStatus(OrderStatus.PENDING);

        String note = request.customerName();
        if (request.phone() != null && !request.phone().isBlank()) {
            note += " · Tlf: " + request.phone();
        }
        if (request.comment() != null && !request.comment().isBlank()) {
            note += " · " + request.comment();
        }
        order.setComment(note);

        if (request.pickupDateTime() != null && !request.pickupDateTime().isBlank()) {
            order.setPickUpTime(LocalDateTime.parse(request.pickupDateTime()));
        }

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = buildOrderItems(savedOrder, request.items()); // ← bruger hjælpemetode
        savedOrder.setOrderItems(orderItems);
        savedOrder.setPrice(orderItems.stream().mapToDouble(OrderItem::getPrice).sum());

        return orderRepository.save(savedOrder);
    }

    // ── Brugerbestilling ─────────────────────────────────────────────────────
    public Order createUserOrder(User user, UserOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Kurven er tom");
        }

        Order order = new Order();
        order.setOrderStatus(OrderStatus.PENDING);
        order.setUser(user);
        if (request.comment() != null && !request.comment().isBlank()) {
            order.setComment(request.comment());
        }
        if (request.pickUpTime() != null && !request.pickUpTime().isBlank()) {
            order.setPickUpTime(LocalDateTime.parse(request.pickUpTime()));
        }

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = buildOrderItems(savedOrder, request.items()); // ← samme hjælpemetode
        savedOrder.setOrderItems(orderItems);
        savedOrder.setPrice(orderItems.stream().mapToDouble(OrderItem::getPrice).sum());

        return orderRepository.save(savedOrder);
    }

    // ── Privat hjælpemetode – bruges af begge flows ──────────────────────────
    private List<OrderItem> buildOrderItems(Order savedOrder, List<OrderItemRequest> items) {
        List<OrderItem> result = new ArrayList<>();
        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new RuntimeException(
                            "Produkt ikke fundet: " + item.productId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.quantity());
            orderItem.setPrice(product.getPrice() * item.quantity()); // pris fra DB, ikke frontend
            result.add(orderItem);
        }
        return result;
    }

    private List<Ingredient> getSelectedIngredients(Product product, GuestOrderRequest.GuestOrderItem item) {
        if (item.selectedIngredients() == null) {
            return product.getIngredients() == null ? List.of() : product.getIngredients();
        }
        if (item.selectedIngredients().isEmpty()) {
            return List.of();
        }

        List<Long> selectedIngredientIds = item.selectedIngredients().stream()
                .map(GuestOrderRequest.SelectedIngredient::id)
                .toList();

        return ingredientRepository.findAllById(selectedIngredientIds);
    }

    private double calculateCustomizedItemPrice(Product product, List<Ingredient> selectedIngredients, int quantity) {
        List<Ingredient> defaultIngredients = product.getIngredients() == null
                ? List.of()
                : product.getIngredients();

        double addedTotal = selectedIngredients.stream()
                .filter(ingredient -> defaultIngredients.stream()
                        .noneMatch(defaultIngredient -> defaultIngredient.getId().equals(ingredient.getId())))
                .mapToDouble(Ingredient::getPrice)
                .sum();

        double removedTotal = defaultIngredients.stream()
                .filter(defaultIngredient -> selectedIngredients.stream()
                        .noneMatch(ingredient -> ingredient.getId().equals(defaultIngredient.getId())))
                .mapToDouble(Ingredient::getPrice)
                .sum();

        double unitPrice = Math.max(0, product.getPrice() + addedTotal - removedTotal);
        return unitPrice * quantity;
    }

    // ── Accept ordre ────────────────────────────────────────────────────
    public Order acceptOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre ikke fundet: " + orderId));

        if (order.getOrderStatus() == OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Ordre #" + orderId + " er allerede accepteret");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Kun ventende ordrer kan accepteres. Nuværende status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ACCEPTED);
        Order savedOrder = orderRepository.save(order);

        // Send email kun hvis ordren har en tilknyttet bruger
        if (order.getUser() != null) {
            emailService.sendOrderAcceptedNotification(
                    order.getUser().getMail(),
                    order.getUser().getName(),
                    order.getId());
        }

        return savedOrder;
    }

    // ── Hent ordrer ─────────────────────────────────────────────────────
    public List<Order> getPendingOrders() {
        return orderRepository.findByOrderStatus(OrderStatus.PENDING);
    }

    public List<Order> getActiveOrders() {
        return orderRepository.findByOrderStatus(OrderStatus.ACCEPTED);
    }

    public java.util.Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    // ── Forudbestilling ─────────────────────────────────────────────────
    public Order createOrderWithPickUpTime(Order order, String pickUpTimeString) {
        Order savedOrder = orderRepository.save(order);
        if (pickUpTimeString != null && !pickUpTimeString.isEmpty()) {
            try {
                java.time.LocalDateTime pickUpDateTime = java.time.LocalDateTime.parse(pickUpTimeString);
                preOrderService.savePickUpTime(savedOrder, pickUpDateTime, true);
            } catch (Exception e) {
                System.err.println("Advarsel: Kunne ikke gemme forudbestilt tidspunkt: " + e.getMessage());
            }
        }
        return savedOrder;
    }

    // ── Ændre eksisterende ordre (admin) ─────────────────────────────────
    public Order updateOrder(Long orderId, UpdateOrderRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre ikke fundet: " + orderId));

        // Kun ACCEPTED ordrer kan redigeres – ikke COMPLETED eller REJECTED
        if (order.getOrderStatus() == OrderStatus.COMPLETED ||
                order.getOrderStatus() == OrderStatus.REJECTED) {
            throw new IllegalStateException(
                    "Ordre #" + orderId + " kan ikke redigeres – status er " + order.getOrderStatus());
        }

        // Opdater kommentar hvis angivet
        if (request.comment() != null && !request.comment().isBlank()) {
            order.setComment(request.comment());
        }

        // Opdater afhentingstidspunkt hvis angivet
        if (request.pickUpTime() != null && !request.pickUpTime().isBlank()) {
            order.setPickUpTime(LocalDateTime.parse(request.pickUpTime()));
        }

        // Opdater varer og antal hvis angivet
        if (request.items() != null && !request.items().isEmpty()) {

            // Ryd eksisterende items (orphanRemoval sletter dem fra databasen)
            order.getOrderItems().clear();

            // Byg nye items
            List<OrderItem> newItems = new ArrayList<>();
            for (UpdateOrderRequest.UpdateOrderItem item : request.items()) {
                Product product = productRepository.findById(item.productId())
                        .orElseThrow(() -> new RuntimeException("Produkt ikke fundet: " + item.productId()));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(item.quantity());
                orderItem.setPrice(product.getPrice() * item.quantity());
                newItems.add(orderItem);
            }

            order.getOrderItems().addAll(newItems);

            // Genberegn totalpris
            double total = newItems.stream().mapToDouble(OrderItem::getPrice).sum();
            order.setPrice(total);
        }

        Order savedOrder = orderRepository.save(order);

        // Notificér kunden hvis ordren har en bruger
        if (order.getUser() != null) {
            emailService.sendOrderUpdatedNotification(
                    order.getUser().getMail(),
                    order.getUser().getName(),
                    order.getId()
            );
        }

        return savedOrder;
    }

    // ── Afvis ordre ─────────────────────────────────────────────────────
    public Order rejectOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre ikke fundet: " + orderId));
        order.setOrderStatus(OrderStatus.REJECTED);
        return orderRepository.save(order);
    }

    //fuldfør ordre
    public Order completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre ikke fundet: " + orderId));

        if (order.getOrderStatus() != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Kun accepterede ordrer kan fuldføres.");
        }

        order.setOrderStatus(OrderStatus.COMPLETED);
        return orderRepository.save(order);
    }


    // ── Salgsstatistik ─────────────────────────────────────────────────
    public SalesStatisticsDto getSalesStatistics(LocalDate from, LocalDate to) {
        List<Order> orders = orderRepository.findSalesOrders(
                List.of(OrderStatus.ACCEPTED, OrderStatus.COMPLETED),
                LocalDateTime.of(from.getYear(), from.getMonth(), from.getDayOfMonth(), 0, 0),
                LocalDateTime.of(to.getYear(), to.getMonth(), to.getDayOfMonth() + 1, 0, 0)
        );

        double totalRevenue = orders.stream()
                .mapToDouble(Order::getPrice)
                .sum();

        var productSales = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getName(),
                        Collectors.summingInt(OrderItem::getQuantity)
                ))
                .entrySet().stream()
                .map(entry -> new ProductSalesDto(null, entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(ProductSalesDto::quantitySold).reversed())
                .limit(5)
                .toList();

        return new SalesStatisticsDto(productSales, totalRevenue);
    }

    // Hent ordrer for den indloggede bruger
    public List<Order> getMyOrders(String email) {
        User user = userRepository.findByMail(email)
                .orElseThrow(() -> new IllegalArgumentException("Bruger ikke fundet"));
        return orderRepository.findOrdersByUserId(user.getId());
    }

    // Hent historik for admin (COMPLETED + rejected, med valgfrit filter)
    public List<Order> getOrderHistory(String statusFilter) {
        List<OrderStatus> statuses = switch (statusFilter == null ? "all" : statusFilter) {
            case "completed" -> List.of(OrderStatus.COMPLETED);
            case "rejected" -> List.of(OrderStatus.REJECTED);
            default          -> List.of(OrderStatus.COMPLETED, OrderStatus.REJECTED);
        };
        // Genbrug den eksisterende findSalesOrders — ingen datofilter her
        return orderRepository.findSalesOrders(statuses, null, null);
    }
}
