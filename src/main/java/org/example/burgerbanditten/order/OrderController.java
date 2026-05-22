package org.example.burgerbanditten.order;

import org.example.burgerbanditten.order.dto.GuestOrderRequest;
import org.example.burgerbanditten.order.dto.SalesStatisticsDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderingSwitchService orderingSwitchService;

    public OrderController(OrderService orderService,
                           OrderingSwitchService orderingSwitchService) {
        this.orderService = orderService;
        this.orderingSwitchService = orderingSwitchService;
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
    public ResponseEntity<?> userCheckout(@RequestBody List<Object> cartItems) {
        if (!orderingSwitchService.isOrdersOpen()) {
            return ResponseEntity.status(503)
                    .body("Bestillinger er midlertidigt lukket – prøv igen senere");
        }
        return ResponseEntity.ok().body("Brugerordre modtaget");
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

    // PUT opdater/ændre ordre
    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(@PathVariable Long orderId, @RequestBody GuestOrderRequest request) {
        try {
            return ResponseEntity.ok(orderService.updateOrder(orderId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
    @GetMapping("/statistics")
    public ResponseEntity<SalesStatisticsDto> getSalesStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(orderService.getSalesStatistics(from, to));
    }
}
