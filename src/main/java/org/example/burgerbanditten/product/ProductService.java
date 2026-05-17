package org.example.burgerbanditten.product;

import org.example.burgerbanditten.ingredient.Ingredient;
import org.example.burgerbanditten.ingredient.IngredientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final IngredientRepository ingredientRepository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, IngredientRepository ingredientRepository, ProductMapper mapper) {
        this.repository = repository;
        this.ingredientRepository = ingredientRepository;
        this.mapper = mapper;
    }

    public List<ProductCardDTO> getProducts() {

        return repository.findAll()
                .stream()
                .map(mapper::toCardDTO)
                .toList();
    }

    public ProductDetailsDTO getProduct(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        return mapper.toDetailsDTO(product);
    }

    public ProductDetailsDTO createProduct(CreateProductDTO dto) {

        Product product = new Product();

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setLunchOffer(dto.lunchOffer());
        product.setCategory(dto.category());


        List<Ingredient> ingredients = ingredientRepository.findAllById(dto.ingredients());
        product.setIngredients(ingredients);

        product.setImage(dto.image());

        Product savedProduct = repository.save(product);

        return mapper.toDetailsDTO(savedProduct);
    }

    public ProductDetailsDTO updateProduct(Long id, UpdateProductDTO dto) {

        Product product = repository.findById(id).orElseThrow(() ->
                new RuntimeException("Product not found"));

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setLunchOffer(dto.lunchOffer());
        product.setCategory(dto.category());

        List<Ingredient> ingredients = ingredientRepository.findAllById(dto.ingredients());
        product.setIngredients(ingredients);

        if (dto.image() != null) {
            product.setImage(dto.image());
        }

        Product updatedProduct = repository.save(product);

        return mapper.toDetailsDTO(updatedProduct);
    }

    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

    public List<Ingredient> getIngredients() {
        return ingredientRepository.findAll();

    }

}
