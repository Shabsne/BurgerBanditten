package org.example.burgerbanditten.order.openinghours;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        return isOpenAt(LocalDateTime.now());
    }

    public NextOpeningDto getNextOpening() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalDateTime now = currentDate.atTime(currentTime);

        if (isOrderingOpen()) {
            return new NextOpeningDto(true, currentDate, null, null, null);
        }

        for (int i = 0; i < 14; i++) {
            LocalDate dateToCheck = currentDate.plusDays(i);

            Optional<NextOpeningDto> holidayOpening = getHolidayOpening(dateToCheck, now);
            if (holidayOpening.isPresent()) {
                return holidayOpening.get();
            }

            Optional<NextOpeningDto> weeklyOpening = getWeeklyOpening(dateToCheck, now);
            if (weeklyOpening.isPresent()) {
                return weeklyOpening.get();
            }
        }
        return new NextOpeningDto(false, null, null, null,
                "Der er ingen kommende åbningstider registreret");
    }

    private Optional<NextOpeningDto> getHolidayOpening(LocalDate dateToCheck, LocalDateTime now) {
        Optional<HolidayOpeningHours> holiday = holidayOpeningHoursRepository.findByDate(dateToCheck);

        if (holiday.isEmpty()) {
            return Optional.empty();
        }

        HolidayOpeningHours h = holiday.get();

        if (!h.isActive() || !dateToCheck.atTime(h.getOpenTime()).isAfter(now)) {
            return Optional.empty();
        }

        return Optional.of(createNextOpeningDto(dateToCheck, h.getOpenTime(), h.getCloseTime()));
    }

    private Optional<NextOpeningDto> getWeeklyOpening(LocalDate dateToCheck, LocalDateTime now) {
        OpeningHours weekly = openingHoursRepository
                .findByDayOfWeek(dateToCheck.getDayOfWeek())
                .orElse(null);

        if (weekly == null || !weekly.isActive()) {
            return Optional.empty();
        }

        if (!dateToCheck.atTime(weekly.getCloseTime()).isAfter(now)) {
            return Optional.empty();
        }

        return Optional.of(createNextOpeningDto(dateToCheck, weekly.getOpenTime(), weekly.getCloseTime()));
    }

    private NextOpeningDto createNextOpeningDto(
            LocalDate date,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        return new NextOpeningDto(
                false,
                date,
                openTime,
                closeTime,
                "Burger Banditten åbner " + date + " kl. " + openTime
        );
    }

    public List<HolidayOpeningHours> getAllHolidayOpeningHours() {
        return holidayOpeningHoursRepository.findAll();
    }

    public HolidayOpeningHours addHolidayOpeningHours(
            String description,
            LocalDate date,
            LocalTime openTime,
            LocalTime closeTime,
            boolean active
    ) {
        HolidayOpeningHours holidayOpeningHours =
                new HolidayOpeningHours(description, date, openTime, closeTime, active);

        return holidayOpeningHoursRepository.save(holidayOpeningHours);
    }

    public void deleteHolidayOpeningHours(Long id) {
        holidayOpeningHoursRepository.deleteById(id);
    }

    // ISSUE #154
    public boolean isOpenAt(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        //Tjek om det er helligdag
        Optional<HolidayOpeningHours> holidayOpeningHours = holidayOpeningHoursRepository.findByDate(date);

        if (holidayOpeningHours.isPresent()) {
            HolidayOpeningHours openingHours = holidayOpeningHours.get();

            if (!openingHours.isActive()) {
                return false;
            }

            return !time.isBefore(openingHours.getOpenTime()) && !time.isAfter(openingHours.getCloseTime());
        }

        OpeningHours openingHours = openingHoursRepository.findByDayOfWeek(date.getDayOfWeek()).orElse(null);

        if (openingHours == null || !openingHours.isActive()) {
            return false;
        }

        return !time.isBefore(openingHours.getOpenTime()) && !time.isAfter(openingHours.getCloseTime());
    }
}