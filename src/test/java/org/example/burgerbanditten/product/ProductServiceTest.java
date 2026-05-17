package org.example.burgerbanditten.product;

import org.example.burgerbanditten.ingredient.Ingredient;
import org.example.burgerbanditten.ingredient.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductService service;

    private Product product;
    private ProductCardDTO cardDTO;
    private ProductDetailsDTO detailsDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Cheese Burger");
        product.setDescription("Burger with cheese");
        product.setPrice(79.0);
        product.setLunchOffer(false);
        product.setCategory(Category.BURGER);
        product.setIngredients(List.of());
        product.setImage(null);

        cardDTO = new ProductCardDTO(1L, "Cheese Burger", "Burger with cheese", 79.0, Category.BURGER, null);

        detailsDTO = new ProductDetailsDTO(1L, "Cheese Burger", "Burger with cheese", 79.0, false, Category.BURGER, List.of());
    }

    // ── getProducts ──────────────────────────────────────────────────────────

    @Test
    void getProducts_returnsListOfCardDTOs() {
        when(repository.findAll()).thenReturn(List.of(product));
        when(mapper.toCardDTO(product)).thenReturn(cardDTO);

        List<ProductCardDTO> result = service.getProducts();

        assertEquals(1, result.size());
        assertEquals("Cheese Burger", result.get(0).name());
    }

    @Test
    void getProducts_returnsEmptyList_whenNoProducts() {
        when(repository.findAll()).thenReturn(List.of());

        List<ProductCardDTO> result = service.getProducts();

        assertTrue(result.isEmpty());
    }

    // ── getProduct ───────────────────────────────────────────────────────────

    @Test
    void getProduct_returnsDetailsDTOById() {
        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(mapper.toDetailsDTO(product)).thenReturn(detailsDTO);

        ProductDetailsDTO result = service.getProduct(1L);

        assertEquals("Cheese Burger", result.name());
        assertEquals(79.0, result.price());
    }

    @Test
    void getProduct_throwsException_whenProductNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getProduct(99L));

        assertEquals("Product not found", ex.getMessage());
    }

    // ── createProduct ────────────────────────────────────────────────────────

    @Test
    void createProduct_savesAndReturnsDetailsDTO() {
        CreateProductDTO dto = new CreateProductDTO(
                "Cheese Burger", "Burger with cheese", 79.0, false, Category.BURGER, List.of(), null
        );

        when(ingredientRepository.findAllById(List.of())).thenReturn(List.of());
        when(repository.save(any(Product.class))).thenReturn(product);
        when(mapper.toDetailsDTO(product)).thenReturn(detailsDTO);

        ProductDetailsDTO result = service.createProduct(dto);

        assertEquals("Cheese Burger", result.name());
        verify(repository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_savesImageWhenProvided() {
        CreateProductDTO dto = new CreateProductDTO(
                "Cheese Burger", "Burger with cheese", 79.0, false, Category.BURGER, List.of(), "data:image/jpeg;base64,abc123"
        );

        when(ingredientRepository.findAllById(List.of())).thenReturn(List.of());
        when(repository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            assertEquals("data:image/jpeg;base64,abc123", saved.getImage());
            return saved;
        });
        when(mapper.toDetailsDTO(any())).thenReturn(detailsDTO);

        service.createProduct(dto);

        verify(repository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_setsIngredientsFromRepository() {
        Ingredient cheese = new Ingredient();
        cheese.setId(1L);
        cheese.setName("Cheese");

        CreateProductDTO dto = new CreateProductDTO(
                "Cheese Burger", "Burger with cheese", 79.0, false, Category.BURGER, List.of(1L), null
        );

        when(ingredientRepository.findAllById(List.of(1L))).thenReturn(List.of(cheese));
        when(repository.save(any(Product.class))).thenReturn(product);
        when(mapper.toDetailsDTO(product)).thenReturn(detailsDTO);

        service.createProduct(dto);

        verify(ingredientRepository).findAllById(List.of(1L));
    }

    // ── updateProduct ────────────────────────────────────────────────────────

    @Test
    void updateProduct_updatesFieldsAndReturnsDetailsDTO() {
        UpdateProductDTO dto = new UpdateProductDTO(
                "Double Burger", "Two patties", 99.0, false, Category.BURGER, List.of(), null
        );

        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(ingredientRepository.findAllById(List.of())).thenReturn(List.of());
        when(repository.save(any(Product.class))).thenReturn(product);
        when(mapper.toDetailsDTO(product)).thenReturn(detailsDTO);

        ProductDetailsDTO result = service.updateProduct(1L, dto);

        assertNotNull(result);
        verify(repository).save(product);
        assertEquals("Double Burger", product.getName());
        assertEquals(99.0, product.getPrice());
    }

    @Test
    void updateProduct_throwsException_whenProductNotFound() {
        UpdateProductDTO dto = new UpdateProductDTO(
                "Double Burger", "Two patties", 99.0, false, Category.BURGER, List.of(), null
        );

        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateProduct(99L, dto));

        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    void updateProduct_updatesImage_whenNewImageProvided() {
        UpdateProductDTO dto = new UpdateProductDTO(
                "Cheese Burger", "Burger with cheese", 79.0, false, Category.BURGER, List.of(), "data:image/jpeg;base64,newimage"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(ingredientRepository.findAllById(List.of())).thenReturn(List.of());
        when(repository.save(any(Product.class))).thenReturn(product);
        when(mapper.toDetailsDTO(product)).thenReturn(detailsDTO);

        service.updateProduct(1L, dto);

        assertEquals("data:image/jpeg;base64,newimage", product.getImage());
    }

    @Test
    void updateProduct_keepsExistingImage_whenNoNewImageProvided() {
        product.setImage("data:image/jpeg;base64,existingimage");

        UpdateProductDTO dto = new UpdateProductDTO(
                "Cheese Burger", "Burger with cheese", 79.0, false, Category.BURGER, List.of(), null
        );

        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(ingredientRepository.findAllById(List.of())).thenReturn(List.of());
        when(repository.save(any(Product.class))).thenReturn(product);
        when(mapper.toDetailsDTO(product)).thenReturn(detailsDTO);

        service.updateProduct(1L, dto);

        assertEquals("data:image/jpeg;base64,existingimage", product.getImage());
    }

    // ── deleteProduct ────────────────────────────────────────────────────────

    @Test
    void deleteProduct_callsRepositoryDeleteById() {
        service.deleteProduct(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_doesNotThrow_whenProductExists() {
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteProduct(1L));
    }
}
