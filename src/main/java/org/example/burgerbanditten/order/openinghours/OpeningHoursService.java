package org.example.burgerbanditten.order.openinghours;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class OpeningHoursService {

    private final OpeningHoursRepository openingHoursRepository;
    private final HolidayOpeningHoursRepository holidayOpeningHoursRepository;

    public OpeningHoursService(OpeningHoursRepository openingHoursRepository, HolidayOpeningHoursRepository holidayOpeningHoursRepository) {
        this.openingHoursRepository = openingHoursRepository;
        this.holidayOpeningHoursRepository = holidayOpeningHoursRepository;
    }

    public OpeningHours getOpeningHoursForToday() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        return openingHoursRepository.findByDayOfWeek(today)
                .orElseGet(() -> openingHoursRepository.save(
                        new OpeningHours(today, LocalTime.of(12, 0), LocalTime.of(22, 0), true)
                ));
    }

    public OpeningHours updateOpeningHours(
            DayOfWeek day,
            LocalTime openTime,
            LocalTime closeTime,
            boolean active
    ) {
        OpeningHours openingHours = openingHoursRepository.findByDayOfWeek(day)
                        .orElseGet(() -> openingHoursRepository.save(createDefaultOpeningHours(day)));

        openingHours.setOpenTime(openTime);
        openingHours.setCloseTime(closeTime);
        openingHours.setActive(active);

        return openingHoursRepository.save(openingHours);
    }

    public List<OpeningHours> getWeeklySchedule() {
        for (DayOfWeek day : DayOfWeek.values()) {
            openingHoursRepository.findByDayOfWeek(day)
                    .orElseGet(() -> openingHoursRepository.save(createDefaultOpeningHours(day)));
        }
        return openingHoursRepository.findAll();
    }

    private OpeningHours createDefaultOpeningHours(DayOfWeek day) {
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
            return new OpeningHours(day, LocalTime.of(12, 0), LocalTime.of(23, 0), true);
        }
        return new OpeningHours(day, LocalTime.of(12, 0), LocalTime.of(22, 0), true);
    }

    public boolean isOrderingOpen() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        Optional<HolidayOpeningHours> holidayOpeningHours = holidayOpeningHoursRepository.findByDate(currentDate);

        if (holidayOpeningHours.isPresent()) {
            HolidayOpeningHours openingHours = holidayOpeningHours.get();

            if (!openingHours.isActive()) {
                return false;
            }

            return !currentTime.isBefore(openingHours.getOpenTime())
                    && !currentTime.isAfter(openingHours.getCloseTime());
        }

        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();

        OpeningHours openingHours =
                openingHoursRepository.findByDayOfWeek(currentDay)
                        .orElse(null);

        if (openingHours == null || !openingHours.isActive()) {
            return false;
        }

        return !currentTime.isBefore(openingHours.getOpenTime())
                && !currentTime.isAfter(openingHours.getCloseTime());
    }
}