package org.example.burgerbanditten.order.openinghours;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Test
    void shouldReturnTrueWhenSelectedTimeIsInsideWeeklyOpeningHours() {
        LocalDateTime pickUpDateTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);

        OpeningHours openingHours = new OpeningHours(
                pickUpDateTime.getDayOfWeek(),
                LocalTime.of(12, 0),
                LocalTime.of(22, 0),
                true
        );

        when(holidayOpeningHoursRepository.findByDate(pickUpDateTime.toLocalDate()))
                .thenReturn(Optional.empty());
        when(openingHoursRepository.findByDayOfWeek(pickUpDateTime.getDayOfWeek()))
                .thenReturn(Optional.of(openingHours));

        assertTrue(openingHoursService.isOpenAt(pickUpDateTime));
    }

    @Test
    void shouldReturnFalseWhenSelectedTimeIsOutsideWeeklyOpeningHours() {
        LocalDateTime pickUpDateTime = LocalDateTime.now().plusDays(1).withHour(23).withMinute(0);

        OpeningHours openingHours = new OpeningHours(
                pickUpDateTime.getDayOfWeek(),
                LocalTime.of(12, 0),
                LocalTime.of(22, 0),
                true
        );

        when(holidayOpeningHoursRepository.findByDate(pickUpDateTime.toLocalDate()))
                .thenReturn(Optional.empty());
        when(openingHoursRepository.findByDayOfWeek(pickUpDateTime.getDayOfWeek()))
                .thenReturn(Optional.of(openingHours));

        assertFalse(openingHoursService.isOpenAt(pickUpDateTime));
    }

    @Test
    void shouldReturnFalseWhenHolidayOpeningHoursAreInactive() {
        LocalDateTime pickUpDateTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        HolidayOpeningHours holidayOpeningHours = new HolidayOpeningHours(
                "Closed special day",
                pickUpDateTime.toLocalDate(),
                LocalTime.of(12, 0),
                LocalTime.of(22, 0),
                false
        );

        when(holidayOpeningHoursRepository.findByDate(pickUpDateTime.toLocalDate()))
                .thenReturn(Optional.of(holidayOpeningHours));

        assertFalse(openingHoursService.isOpenAt(pickUpDateTime));
    }

    @Test
    void shouldUseHolidayOpeningHoursBeforeWeeklyOpeningHours() {
        LocalDateTime pickUpDateTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        HolidayOpeningHours holidayOpeningHours = new HolidayOpeningHours(
                "Special opening hours",
                pickUpDateTime.toLocalDate(),
                LocalTime.of(17, 0),
                LocalTime.of(20, 0),
                true
        );

        when(holidayOpeningHoursRepository.findByDate(pickUpDateTime.toLocalDate()))
                .thenReturn(Optional.of(holidayOpeningHours));

        assertTrue(openingHoursService.isOpenAt(pickUpDateTime));
    }
}
