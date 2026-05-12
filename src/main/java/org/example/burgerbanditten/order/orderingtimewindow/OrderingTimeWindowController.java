package org.example.burgerbanditten.order.orderingtimewindow;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Controller
@RequestMapping("/admin/order-window")
public class OrderingTimeWindowController {

    private final OrderingTimeWindowService orderingTimeWindowService;

    public OrderingTimeWindowController(OrderingTimeWindowService orderingTimeWindowService) {
        this.orderingTimeWindowService = orderingTimeWindowService;
    }

    @GetMapping
    public String showOrderingWindowPage(Model model) {
        model.addAttribute("weeklySchedule", orderingTimeWindowService.getWeeklySchedule());
        return "admin-order-window";
    }

    @PostMapping("/api")
    @ResponseBody
    public OrderingTimeWindow updateOrderingWindowApi(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam String openTime,
            @RequestParam String closeTime,
            @RequestParam(required = false) boolean active
    ) {

        return orderingTimeWindowService.updateOrderingWindow(
                dayOfWeek,
                LocalTime.parse(openTime),
                LocalTime.parse(closeTime),
                active
        );
    }
}