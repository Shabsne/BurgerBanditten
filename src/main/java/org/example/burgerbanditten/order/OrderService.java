package org.example.burgerbanditten.order;

import org.example.burgerbanditten.email.EmailService;
import org.example.burgerbanditten.order.dto.ProductSalesDto;
import org.example.burgerbanditten.order.dto.SalesStatisticsDto;
import org.example.burgerbanditten.preorder.PreOrderService;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final PreOrderService preOrderService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, EmailService emailService, PreOrderService preOrderService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
        this.preOrderService = preOrderService;
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
