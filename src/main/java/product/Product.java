package product;

import Ingredient.Ingredient;
import jakarta.persistence.*;


import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Double price;

    private Boolean lunchOffer;

    @Enumerated(EnumType.STRING)
    private Size size;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToMany
    @JoinTable(
            name = "product_ingredients",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private List<Ingredient> ingredients;

    public Product() {}

    public Product(Long id, String name, String description, Double price, Boolean lunchOffer, Size size, Category category, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.lunchOffer = lunchOffer;
        this.size = size;
        this.category = category;
        this.ingredients = ingredients;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }
    public Boolean getLunchOffer() { return lunchOffer; }
    public Size getSize() { return size; }
    public Category getCategory() { return category; }
    public List<Ingredient> getIngredients() { return ingredients; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Double price) { this.price = price; }
    public void setLunchOffer(Boolean lunchOffer) { this.lunchOffer = lunchOffer; }
    public void setSize(Size size) { this.size = size; }
    public void setCategory(Category category) { this.category = category; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }
}
