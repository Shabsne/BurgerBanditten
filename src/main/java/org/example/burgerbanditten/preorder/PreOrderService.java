package org.example.burgerbanditten.preorder;

import org.example.burgerbanditten.order.Order;
import org.example.burgerbanditten.order.openinghours.NextOpeningDto;
import org.example.burgerbanditten.order.openinghours.OpeningHoursService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PreOrderService {
    private final PickUpTimeRepository pickUpTimeRepository;
    private final OpeningHoursService openingHoursService;

    public PreOrderService(
            PickUpTimeRepository pickUpTimeRepository,
            OpeningHoursService openingHoursService
    ) {
        this.pickUpTimeRepository = pickUpTimeRepository;
        this.openingHoursService = openingHoursService;
    }

    public boolean isValidPickUpTime(LocalDateTime pickUpDateTime) {
        if (pickUpDateTime == null) {
            throw new IllegalArgumentException("Afhentningstidspunkt må ikke være null");
        }

        if (pickUpDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Kan ikke vælge tidspunkt i fortiden");
        }

        if (!openingHoursService.isOpenAt(pickUpDateTime)) {
            throw new IllegalArgumentException("Valgt afhentningstidspunkt er uden for aabningstiden");
        }

        return true;
    }

    public PickUpTime savePickUpTime(Order order,LocalDateTime pickUpDateTime, boolean customerSelected) {
        if (!isValidPickUpTime(pickUpDateTime)) {
            throw new IllegalArgumentException("Ugyldigt afhentningstidspunkt");
        }

        PickUpTime pickUpTime = new PickUpTime(order, pickUpDateTime, customerSelected);
        return pickUpTimeRepository.save(pickUpTime);
    }

    public PickUpTime getPickUpTimeForOrder(Long orderId) {
        return pickUpTimeRepository.findByOrderId(orderId);
    }

    public NextOpeningDto getNextAvailablePickup() {
        return openingHoursService.getNextOpening();
    }
}
