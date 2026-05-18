package org.example.burgerbanditten.preorder;

import org.example.burgerbanditten.order.Order;
import org.example.burgerbanditten.order.openinghours.OpeningHoursService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreOrderServiceTest {

    @Mock
    private PickUpTimeRepository pickUpTimeRepository;

    @Mock
    private OpeningHoursService openingHoursService;

    @InjectMocks
    private PreOrderService preOrderService;

    @Test
    void shouldAcceptPickupTimeWhenItIsInFutureAndInsideOpeningHours() {
        LocalDateTime pickUpDateTime = LocalDateTime.now().plusDays(1);

        when(openingHoursService.isOpenAt(pickUpDateTime)).thenReturn(true);

        assertTrue(preOrderService.isValidPickUpTime(pickUpDateTime));
    }

    @Test
    void shouldRejectNullPickupTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> preOrderService.isValidPickUpTime(null)
        );

        verify(openingHoursService, never()).isOpenAt(any());
    }

    @Test
    void shouldRejectPickupTimeInThePast() {
        LocalDateTime pickUpDateTime = LocalDateTime.now().minusHours(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> preOrderService.isValidPickUpTime(pickUpDateTime)
        );

        verify(openingHoursService, never()).isOpenAt(any());
    }

    @Test
    void shouldRejectPickupTimeOutsideOpeningHours() {
        LocalDateTime pickUpDateTime = LocalDateTime.now().plusDays(1);

        when(openingHoursService.isOpenAt(pickUpDateTime)).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> preOrderService.isValidPickUpTime(pickUpDateTime)
        );
    }

    @Test
    void shouldSavePickupTimeWhenPickupTimeIsValid() {
        Order order = new Order();
        LocalDateTime pickUpDateTime = LocalDateTime.now().plusDays(1);

        when(openingHoursService.isOpenAt(pickUpDateTime)).thenReturn(true);
        when(pickUpTimeRepository.save(any(PickUpTime.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PickUpTime savedPickUpTime = preOrderService.savePickUpTime(order, pickUpDateTime, true);

        assertSame(order, savedPickUpTime.getOrder());
        assertEquals(pickUpDateTime, savedPickUpTime.getPickupDateTime());
        assertTrue(savedPickUpTime.isCustomerSelected());
    }
}
