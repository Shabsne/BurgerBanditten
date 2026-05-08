package order;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import user.User;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> orderItems;

    private double price;

    private String comment;

    private LocalDateTime pickUpTime;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    public Order() {}

    public Order(Long id, User user, List<OrderItem> orderItems, double price, String comment, LocalDateTime pickUpTime, OrderStatus orderStatus) {
        this.id = id;
        this.user = user;
        this.orderItems = orderItems;
        this.price = price;
        this.comment = comment;
        this.pickUpTime = pickUpTime;
        this.orderStatus = orderStatus;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public double getPrice() { return price; }
    public String getComment() { return comment; }
    public LocalDateTime getPickUpTime() { return pickUpTime; }
    public OrderStatus getOrderStatus() { return orderStatus; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    public void setPrice(double price) { this.price = price; }
    public void setComment(String comment) { this.comment = comment; }
    public void setPickUpTime(LocalDateTime pickUpTime) { this.pickUpTime = pickUpTime; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
}

