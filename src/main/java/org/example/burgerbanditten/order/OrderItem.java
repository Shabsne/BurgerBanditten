package org.example.burgerbanditten.order;

import org.example.burgerbanditten.ingredient.Ingredient;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.example.burgerbanditten.product.Product;


import java.util.List;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    private double price;

    @ManyToMany
    @JoinTable(
            name = "order_item_ingredients",
            joinColumns = @JoinColumn(name = "order_item_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private List<Ingredient> selectedIngredients;

    public OrderItem() {}

    public OrderItem(Long id, Order order, Product product, int quantity, double price, List<Ingredient> selectedIngredients) {
        this.id = id;
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.selectedIngredients = selectedIngredients;
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public List<Ingredient> getSelectedIngredients() { return selectedIngredients; }

    public void setId(Long id) { this.id = id; }
    public void setOrder(Order order) { this.order = order; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }
    public void setSelectedIngredients(List<Ingredient> selectedIngredients) { this.selectedIngredients = selectedIngredients; }
}

