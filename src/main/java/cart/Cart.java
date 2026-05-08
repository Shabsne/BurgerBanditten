package cart;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import user.User;


import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CartItem> cartItems;

    public Cart() {}

    public Cart(Long id, User user, List<CartItem> cartItems) {
        this.id = id;
        this.user = user;
        this.cartItems = cartItems;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public List<CartItem> getCartItems() { return cartItems; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setCartItems(List<CartItem> cartItems) { this.cartItems = cartItems; }
}

