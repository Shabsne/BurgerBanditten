package org.example.burgerbanditten.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders") // RETTET: Tilføjet /api så det matcher frontend og security
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Modtag gæstebestilling via checkout
    @PostMapping("/guest/checkout")
    public ResponseEntity<?> guestCheckout(@RequestBody List<Object> cartItems) {
        // Logik til at håndtere gæstebestilling via din orderService
        // F.eks: orderService.createGuestOrder(cartItems);
        return ResponseEntity.ok().body("Gæsteordre modtaget");
    }

    // Modtag logget ind bruger bestilling via checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> userCheckout(@RequestBody List<Object> cartItems) {
        // Logik til at håndtere brugerbestilling via din orderService
        return ResponseEntity.ok().body("Brugerordre modtaget");
    }

    // #124 – Endpoint som admin kalder for at acceptere en ordre
    // #127 – Returnerer den opdaterede ordre som bekræftelse til admin
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

    // Frontend: hent ventende ordrer til tab "Ventende"
    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        return ResponseEntity.ok(orderService.getPendingOrders());
    }

    // #126 – Hent alle aktive (accepterede) ordrer
    @GetMapping("/active")
    public ResponseEntity<List<Order>> getActiveOrders() {
        return ResponseEntity.ok(orderService.getActiveOrders());
    }
}