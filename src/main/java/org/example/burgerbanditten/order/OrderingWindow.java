package org.example.burgerbanditten.order;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class OrderingWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean active;

    public OrderingWindow() {
    }

    public OrderingWindow(LocalTime openTime, LocalTime closeTime, boolean active) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}