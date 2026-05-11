package org.example.burgerbanditten.order.orderingwindow;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Controller
@RequestMapping("/admin/order-window")
public class OrderingWindowController {

    private final OrderingWindowService orderingWindowService;

    public OrderingWindowController(OrderingWindowService orderingWindowService) {
        this.orderingWindowService = orderingWindowService;
    }

    @GetMapping
    public String showOrderingWindowPage(Model model) {
        model.addAttribute("weeklySchedule", orderingWindowService.getWeeklySchedule());
        return "admin-order-window";
    }

    @PostMapping
    public String updateOrderingWindow(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam String openTime,
            @RequestParam String closeTime,
            @RequestParam(required = false) boolean active
    ) {
        orderingWindowService.updateOrderingWindow(
                dayOfWeek,
                LocalTime.parse(openTime),
                LocalTime.parse(closeTime),
                active
        );

        return "redirect:/admin/order-window";
    }
}