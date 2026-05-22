package org.example.burgerbanditten.order;

import org.springframework.stereotype.Service;

@Service
public class OrderingSwitchService {

    // Bestillinger er åbne som standard
    private boolean ordersOpen = true;

    // Hent nuværende status
    public boolean isOrdersOpen() {
        return ordersOpen;
    }

    // Åbn for bestillinger
    public void openOrders() {
        ordersOpen = true;
    }

    // Luk for bestillinger
    public void closeOrders() {
        ordersOpen = false;
    }

    // Toggle – skifter status med ét klik
    public boolean toggleOrders() {
        ordersOpen = !ordersOpen;
        return ordersOpen;
    }
}