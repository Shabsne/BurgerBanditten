package org.example.burgerbanditten.order.openinghours;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpeningHoursPublicController {

    private final OpeningHoursService openingHoursService;

    public OpeningHoursPublicController(OpeningHoursService openingHoursService) {
        this.openingHoursService = openingHoursService;
    }

    @GetMapping("/opening-hours/next")
    public NextOpeningDto getNextOpening() {
        return openingHoursService.getNextOpening();
    }
}
