package org.example.burgerbanditten.ingredient;

import jakarta.persistence.*;

@Entity
@Table(name = "ingredients")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private double price;

    private int inventory;

    private boolean addOn;

    public Ingredient() {}

    public Ingredient(Long id, String name, double price, int inventory, boolean addOn) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.inventory = inventory;
        this.addOn = addOn;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getInventory() { return inventory; }
    public boolean isAddOn() { return addOn; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setInventory(int inventory) { this.inventory = inventory; }
    public void setAddOn(boolean addOn) { this.addOn = addOn; }
}
