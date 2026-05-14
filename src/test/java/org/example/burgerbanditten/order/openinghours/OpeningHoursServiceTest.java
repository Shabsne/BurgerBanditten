package org.example.burgerbanditten.order.openinghours;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class OpeningHoursServiceTest {

    @Mock
    private OpeningHoursRepository openingHoursRepository;

    @Mock
    private HolidayOpeningHoursRepository holidayOpeningHoursRepository;

    @InjectMocks
    private OpeningHoursService openingHoursService;

    @Test
    void shouldReturnFalseWhenRestaurantIsClosed() {
        OpeningHours openingHours = new OpeningHours(
                LocalDate.now().getDayOfWeek(),
                LocalTime.of(1, 0),
                LocalTime.of(2, 0),
                true
        );

        when(openingHoursRepository.findByDayOfWeek(LocalDate.now().getDayOfWeek()))
                .thenReturn(Optional.of(openingHours));

        when(holidayOpeningHoursRepository.findByDate(LocalDate.now()))
                .thenReturn(Optional.empty());

        assertFalse(openingHoursService.isOrderingOpen());
    }

    @Test
    void shouldReturnTrueWhenRestaurantIsOpen() {
        LocalTime now = LocalTime.now();

        OpeningHours openingHours = new OpeningHours(
                LocalDate.now().getDayOfWeek(),
                now.minusHours(1),
                now.plusHours(1),
                true
        );

        when(openingHoursRepository.findByDayOfWeek(LocalDate.now().getDayOfWeek()))
                .thenReturn(Optional.of(openingHours));

        when(holidayOpeningHoursRepository.findByDate(LocalDate.now()))
                .thenReturn(Optional.empty());

        assertTrue(openingHoursService.isOrderingOpen());
    }
}
