package org.example.burgerbanditten.order;

import org.example.burgerbanditten.email.EmailService;
import org.example.burgerbanditten.order.dto.GuestOrderRequest;
import org.example.burgerbanditten.order.dto.ProductSalesDto;
import org.example.burgerbanditten.order.dto.SalesStatisticsDto;
import org.example.burgerbanditten.preorder.PreOrderService;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final PreOrderService preOrderService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        EmailService emailService,
                        PreOrderService preOrderService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
        this.preOrderService = preOrderService;
    }

    // ── Gæsteordre ─────────────────────────────────────────────────────
    // Opretter en rigtig Order i databasen med status PENDING,
    // så den dukker op på admin-sidens "Ventende"-fane.
    public Order createGuestOrder(GuestOrderRequest request) {

        // Byg ordre
        Order order = new Order();
        order.setOrderStatus(OrderStatus.PENDING);

        // Gem kundenavn og telefon i kommentarfeltet (User er null for gæster)
        String note = request.customerName();
        if (request.phone() != null && !request.phone().isBlank()) {
            note += " · Tlf: " + request.phone();
        }
        if (request.comment() != null && !request.comment().isBlank()) {
            note += " · " + request.comment();
        }
        order.setComment(note);

        // Gem ordren først for at få et ID (nødvendigt for FK på OrderItem)
        Order savedOrder = orderRepository.save(order);

        // Byg ordre-linjer
        List<OrderItem> orderItems = new ArrayList<>();
        for (GuestOrderRequest.GuestOrderItem item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new RuntimeException(
                            "Produkt ikke fundet: " + item.productId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.quantity());
            // Brug produktets aktuelle pris — ikke frontenden's (sikkerhed)
            orderItem.setPrice(product.getPrice() * item.quantity());
            orderItems.add(orderItem);
        }

        // Sæt items og beregn total
        savedOrder.setOrderItems(orderItems);
        double total = orderItems.stream()
                .mapToDouble(OrderItem::getPrice)
                .sum();
        savedOrder.setPrice(total);

        return orderRepository.save(savedOrder);
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

    // #126 – Frontend: tab "Aktive"
    public List<Order> getActiveOrders() {
        return orderRepository.findByOrderStatus(OrderStatus.ACCEPTED);
    }

    // ── Forudbestilling ─────────────────────────────────────────────────
    public Order createOrderWithPickUpTime(Order order, String pickUpTimeString) {
        //Gem ordren først
        Order savedOrder = orderRepository.save(order);

        //Hvis der er et forudbestilt tidspunkt, gem det
        if (pickUpTimeString != null && !pickUpTimeString.isEmpty()) {
            try {
                java.time.LocalDateTime pickUpDateTime =
                        java.time.LocalDateTime.parse(pickUpTimeString);

                preOrderService.savePickUpTime(savedOrder, pickUpDateTime, true);
            } catch (Exception e) {
                //Log fejl - forudbestilling mislykkedes men ordren er skabt
                System.err.println("Advarsel: Kunne ikke gemme forudbestilt tidspunkt: " + e.getMessage() );
            }
        }
        return savedOrder;
    }

    // ── Salgsstatistik ─────────────────────────────────────────────────
    public SalesStatisticsDto getSalesStatistics(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.plusDays(1).atStartOfDay();

        List<Order> orders = orderRepository.findSalesOrders(
                List.of(OrderStatus.ACCEPTED, OrderStatus.COMPLETED),
                fromDateTime,
                toDateTime
        );

        Map<Long, ProductSalesDto> productSales = new LinkedHashMap<>();
        for (Product product : productRepository.findAll()) {
            productSales.put(product.getId(), new ProductSalesDto(product.getId(), product.getName(), 0));
        }

        double totalRevenue = 0;
        for (Order order : orders) {
            totalRevenue += order.getPrice();

            if (order.getOrderItems() == null) {
                continue;
            }

            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product == null) {
                    continue;
                }

                ProductSalesDto current = productSales.getOrDefault(
                        product.getId(),
                        new ProductSalesDto(product.getId(), product.getName(), 0)
                );
                productSales.put(
                        product.getId(),
                        new ProductSalesDto(product.getId(), current.productName(), current.quantitySold() + item.getQuantity())
                );
            }
        }

        return new SalesStatisticsDto(List.copyOf(productSales.values()), totalRevenue);
    }
}
