package org.example.burgerbanditten.order;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
 class OrderingSwitchTest {

    // Vi tester OrderingSwitchService direkte
    // da logikken er simpel og ikke afhænger af andre klasser
    private OrderingSwitchService orderingSwitchService;

    @BeforeEach
    void setUp() {
        orderingSwitchService = new OrderingSwitchService();
    }

    // ── Standard tilstand ────────────────────────────────

    @Test
    void skalVaereAaben_SomStandard() {
        assertTrue(orderingSwitchService.isOrdersOpen());
    }

    // ── Luk bestillinger ─────────────────────────────────

    @Test
    void skalLukkeForBestillinger() {
        orderingSwitchService.closeOrders();

        assertFalse(orderingSwitchService.isOrdersOpen());
    }

    // ── Åbn bestillinger ─────────────────────────────────

    @Test
    void skalAabneForBestillinger_EfterAtVaereLukket() {
        orderingSwitchService.closeOrders();
        orderingSwitchService.openOrders();

        assertTrue(orderingSwitchService.isOrdersOpen());
    }

    // ── Toggle ───────────────────────────────────────────

    @Test
    void skalToggleTilLukket_NaarAaben() {
        // Standard er åben – toggle skal lukke
        boolean result = orderingSwitchService.toggleOrders();

        assertFalse(result);
        assertFalse(orderingSwitchService.isOrdersOpen());
    }

    @Test
    void skalToggleTilAaben_NaarLukket() {
        orderingSwitchService.closeOrders();

        // Toggle skal åbne igen
        boolean result = orderingSwitchService.toggleOrders();

        assertTrue(result);
        assertTrue(orderingSwitchService.isOrdersOpen());
    }

    @Test
    void skalStatusAendresSigOejeblikkeligt() {
        // Bekræft at status ændres med det samme uden forsinkelse
        assertTrue(orderingSwitchService.isOrdersOpen());

        orderingSwitchService.closeOrders();
        assertFalse(orderingSwitchService.isOrdersOpen());

        orderingSwitchService.openOrders();
        assertTrue(orderingSwitchService.isOrdersOpen());
    }
}

