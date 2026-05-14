package org.example.burgerbanditten.order.openinghours;

import java.time.LocalDate;
import java.time.LocalTime;

public record NextOpeningDto(
        boolean openNow,
        LocalDate nextDate,
        LocalTime nextOpenTime,
        LocalTime nextCloseTime,
        String message
) {
}
