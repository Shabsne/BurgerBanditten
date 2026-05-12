package org.example.burgerbanditten.order.openinghours;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/admin/opening-hours")
public class OpeningHoursController {

    private final OpeningHoursService openingHoursService;

    public OpeningHoursController(OpeningHoursService openingHoursService) {
        this.openingHoursService = openingHoursService;
    }

    @GetMapping
    public String showOpeningHoursPage(Model model) {
        model.addAttribute("weeklySchedule", openingHoursService.getWeeklySchedule());
        return "admin-opening-hours";
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

    @PostMapping("/holiday")
    public String addHolidayOpeningHours(
            @RequestParam String description,
            @RequestParam String date,
            @RequestParam String openTime,
            @RequestParam String closeTime,
            @RequestParam(required = false) boolean active
    ) {
        openingHoursService.addHolidayOpeningHours(
                description,
                LocalDate.parse(date),
                LocalTime.parse(openTime),
                LocalTime.parse(closeTime),
                active
        );

        return "redirect:/admin/opening-hours";
    }

    @PostMapping("/holiday/delete")
    public String deleteHolidayOpeningHours(@RequestParam Long id) {
        openingHoursService.deleteHolidayOpeningHours(id);

        return "redirect:/admin/opening-hours";
    }
}