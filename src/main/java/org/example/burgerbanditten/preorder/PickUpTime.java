package org.example.burgerbanditten.preorder;

import jakarta.persistence.*;
import org.example.burgerbanditten.order.Order;

import java.time.LocalDateTime;

@Entity
@Table(name = "pickup_times")
public class PickUpTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private LocalDateTime pickupDateTime;

    // Var dette valgt af kunden eller system-default
    private boolean customerSelected;

    public PickUpTime() {}

    public PickUpTime(Order order, LocalDateTime pickupDateTime, boolean customerSelected) {
        this.order = order;
        this.pickupDateTime = pickupDateTime;
        this.customerSelected = customerSelected;
    }

    public Long getId() {
        return id; }

    public Order getOrder() {
        return order; }

    public LocalDateTime getPickupDateTime() {
        return pickupDateTime; }

    public boolean isCustomerSelected() {
        return customerSelected; }

    public void setId(Long id) {
        this.id = id; }

    public void setOrder(Order order) {
        this.order = order; }

    public void setPickupDateTime(LocalDateTime pickupDateTime) {
        this.pickupDateTime = pickupDateTime; }

    public void setCustomerSelected(boolean customerSelected) {
        this.customerSelected = customerSelected; }
}

