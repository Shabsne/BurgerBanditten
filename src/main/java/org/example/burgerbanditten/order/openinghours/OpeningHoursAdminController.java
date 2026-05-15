package org.example.burgerbanditten.order.openinghours;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/admin/opening-hours/api")
public class OpeningHoursAdminController {

    private final OpeningHoursService openingHoursService;

    public OpeningHoursAdminController(OpeningHoursService openingHoursService) {
        this.openingHoursService = openingHoursService;
    }

    @GetMapping("/weekly")
    public List<OpeningHours> getWeeklySchedule() {
        return openingHoursService.getWeeklySchedule();
    }

    @PostMapping("/weekly")
    public OpeningHours updateOpeningHours(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam String openTime,
            @RequestParam String closeTime,
            @RequestParam boolean active
    ) {
        return openingHoursService.updateOpeningHours(
                dayOfWeek,
                LocalTime.parse(openTime),
                LocalTime.parse(closeTime),
                active
        );
    }

    @GetMapping("/holidays")
    public List<HolidayOpeningHours> getHolidays() {
        return openingHoursService.getAllHolidayOpeningHours();
    }

    @PostMapping("/holidays")
    public HolidayOpeningHours addHolidayOpeningHours(
            @RequestParam String description,
            @RequestParam String date,
            @RequestParam String openTime,
            @RequestParam String closeTime,
            @RequestParam boolean active
    ) {
        return openingHoursService.addHolidayOpeningHours(
                description,
                LocalDate.parse(date),
                LocalTime.parse(openTime),
                LocalTime.parse(closeTime),
                active
        );
    }

    @DeleteMapping("/holidays/{id}")
    public void deleteHolidayOpeningHours(@PathVariable Long id) {
        openingHoursService.deleteHolidayOpeningHours(id);
    }
}
