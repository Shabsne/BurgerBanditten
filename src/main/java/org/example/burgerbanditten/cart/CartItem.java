package org.example.burgerbanditten.cart;

import org.example.burgerbanditten.ingredient.Ingredient;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.example.burgerbanditten.product.Product;


import java.util.List;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonBackReference
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @ManyToMany
    @JoinTable(
            name = "cart_item_ingredients",
            joinColumns = @JoinColumn(name = "cart_item_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private List<Ingredient> selectedIngredients;

    public CartItem() {}

    public CartItem(Long id, Cart cart, Product product, int quantity, List<Ingredient> selectedIngredients) {
        this.id = id;
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.selectedIngredients = selectedIngredients;
    }

    public Long getId() { return id; }
    public Cart getCart() { return cart; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public List<Ingredient> getSelectedIngredients() { return selectedIngredients; }

    public void setId(Long id) { this.id = id; }
    public void setCart(Cart cart) { this.cart = cart; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setSelectedIngredients(List<Ingredient> selectedIngredients) { this.selectedIngredients = selectedIngredients; }
}
