package org.example.burgerbanditten.order.openinghours;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Controller
@RequestMapping("/admin/order-window")
public class OpeningHoursController {

    private final OpeningHoursService openingHoursService;

    public OpeningHoursController(OpeningHoursService openingHoursService) {
        this.openingHoursService = openingHoursService;
    }

    @GetMapping
    public String showOrderingWindowPage(Model model) {
        model.addAttribute("weeklySchedule", openingHoursService.getWeeklySchedule());
        return "admin-order-window";
    }

    @PostMapping("/api")
    @ResponseBody
    public OpeningHours updateOrderingWindowApi(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam String openTime,
            @RequestParam String closeTime,
            @RequestParam(required = false) boolean active
    ) {

        return openingHoursService.updateOpeningHours(
                dayOfWeek,
                LocalTime.parse(openTime),
                LocalTime.parse(closeTime),
                active
        );
    }
}