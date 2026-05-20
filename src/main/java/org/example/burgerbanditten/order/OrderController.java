package org.example.burgerbanditten.order;

import org.example.burgerbanditten.order.dto.GuestOrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST gæstebestilling — opretter en rigtig ordre med PENDING status
    // Vises herefter på admin-sidens "Ventende"-fane
    @PostMapping("/guest/checkout")
    public ResponseEntity<?> guestCheckout(@RequestBody GuestOrderRequest request) {
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

    // POST logget-ind bruger bestilling
    @PostMapping("/checkout")
    public ResponseEntity<?> userCheckout(@RequestBody List<Object> cartItems) {
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
}