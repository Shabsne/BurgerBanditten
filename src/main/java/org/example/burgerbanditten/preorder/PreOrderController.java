package org.example.burgerbanditten.preorder;

import org.example.burgerbanditten.order.openinghours.NextOpeningDto;
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
    public ResponseEntity<ValidationResponse> validatePickUpTime(@RequestBody PreOrderRequest request) {
        try {
            boolean isValid = preOrderService.isValidPickUpTime(request.pickUpDateTime());
            return ResponseEntity.ok(new ValidationResponse(isValid, "Tidspunkt er gyldigt"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ValidationResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/next-available")
    public ResponseEntity<NextOpeningDto> getNextAvailable() {
            return ResponseEntity.ok(preOrderService.getNextAvailablePickup());
    }
}

