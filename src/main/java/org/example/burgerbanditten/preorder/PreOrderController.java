package org.example.burgerbanditten.preorder;

import org.example.burgerbanditten.preorder.dto.PreOrderRequest;
import org.example.burgerbanditten.preorder.dto.ValidationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preorder")
public class PreOrderController {

    private final PreOrderService preOrderService;

    public PreOrderController(PreOrderService preOrderService) {
        this.preOrderService = preOrderService;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validatePickUpTime(@RequestBody PreOrderRequest request) {
        try {
            boolean isValid = preOrderService.isValidPickUpTime(request.pickUpDateTime());
            return ResponseEntity.ok(new ValidationResponse(isValid, "Tidspunkt er gyldigt"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ValidationResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/next-available")
    public ResponseEntity<?> getNextAvailable() {
        try {
            var nextOpening = preOrderService.getNextAvailablePickup();
            return ResponseEntity.ok(nextOpening);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Kunne ikke hente næste åbningstid");
        }
    }

    public record ValidationResponse(boolean valid, String message) {}
}
