package org.example.burgerbanditten.product;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
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
}
