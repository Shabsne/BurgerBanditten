package org.example.burgerbanditten.order;

import org.example.burgerbanditten.order.dto.GuestOrderRequest;
import org.example.burgerbanditten.order.dto.SalesStatisticsDto;
import org.example.burgerbanditten.order.dto.UpdateOrderRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.burgerbanditten.order.dto.UserOrderRequest;
import org.example.burgerbanditten.user.User;
import org.example.burgerbanditten.user.UserRepository;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderingSwitchService orderingSwitchService;

    public OrderController(OrderService orderService,
                           OrderingSwitchService orderingSwitchService,
                           UserRepository userRepository) {
        this.orderService = orderService;
        this.orderingSwitchService = orderingSwitchService;
        this.userRepository = userRepository;
    }

    // GET – hent nuværende bestillingsstatus (til frontend)
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getOrderingStatus() {
        return ResponseEntity.ok(Map.of("open", orderingSwitchService.isOrdersOpen()));
    }

    // POST – toggle bestillings-switch (kun admin)
    @PostMapping("/admin/toggle")
    public ResponseEntity<Map<String, Object>> toggleOrdering() {
        boolean isNowOpen = orderingSwitchService.toggleOrders();
        return ResponseEntity.ok(Map.of(
                "open", isNowOpen,
                "message", isNowOpen ? "Bestillinger er nu åbne" : "Bestillinger er nu lukkede"
        ));
    }


    // GET – hent én specifik ordre (admin – bruges til rediger modal)
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT – ændr en eksisterende ordre (kun admin)
    @PutMapping("/{orderId}/update")
    public ResponseEntity<?> updateOrder(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderRequest request) {
        try {
            Order updated = orderService.updateOrder(orderId, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST gæstebestilling – tjekker switch inden bestilling oprettes
    @PostMapping("/guest/checkout")
    public ResponseEntity<?> guestCheckout(@RequestBody GuestOrderRequest request) {
        if (!orderingSwitchService.isOrdersOpen()) {
            return ResponseEntity.status(503)
                    .body("Bestillinger er midlertidigt lukket – prøv igen senere");
        }
        try {
            if (request.items() == null || request.items().isEmpty()) {
                return ResponseEntity.badRequest().body("Kurven er tom");
            }
            Order order = orderService.createGuestOrder(request);
            return ResponseEntity.ok(order.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST logget-ind bruger bestilling – tjekker også switch
    @PostMapping("/checkout")
    public ResponseEntity<?> userCheckout(
            @RequestBody UserOrderRequest request,
            Authentication authentication) {

        if (!orderingSwitchService.isOrdersOpen()) {
            return ResponseEntity.status(503)
                    .body("Bestillinger er midlertidigt lukket – prøv igen senere");
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Ikke logget ind");
        }
        try {
            String mail = authentication.getName();
            User user = userRepository.findByMail(mail)
                    .orElseThrow(() -> new RuntimeException("Bruger ikke fundet"));
            Order order = orderService.createUserOrder(user, request);
            return ResponseEntity.ok(order.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT accepter ordre (kun admin)
    @PutMapping("/{orderId}/accept")
    public ResponseEntity<?> acceptOrder(@PathVariable Long orderId) {
        try {
            Order accepted = orderService.acceptOrder(orderId);
            return ResponseEntity.ok(accepted);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET ventende ordrer (kun admin)
    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        return ResponseEntity.ok(orderService.getPendingOrders());
    }

    // GET aktive ordrer (kun admin)
    @GetMapping("/active")
    public ResponseEntity<List<Order>> getActiveOrders() {
        return ResponseEntity.ok(orderService.getActiveOrders());
    }

    // PUT afvis ordre
    @PutMapping("/{orderId}/reject")
    public ResponseEntity<?> rejectOrder(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(orderService.rejectOrder(orderId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT fuldfør ordre
    @PutMapping("/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(orderService.completeOrder(orderId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET mine ordrer — kræver at bruger er logget ind
    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Order> orders = orderService.getMyOrders(authentication.getName());
        return ResponseEntity.ok(orders);
    }

    // GET historik — kun admin
// ?status=completed | cancelled | (tom = begge)
    @GetMapping("/history")
    public ResponseEntity<List<Order>> getOrderHistory(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getOrderHistory(status));
    }


    @GetMapping("/statistics")
    public ResponseEntity<SalesStatisticsDto> getSalesStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(orderService.getSalesStatistics(from, to));
    }
}
