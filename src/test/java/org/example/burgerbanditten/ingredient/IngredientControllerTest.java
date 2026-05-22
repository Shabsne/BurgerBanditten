package org.example.burgerbanditten.ingredient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientControllerTest {

    @Mock
    private IngredientService service;

    @InjectMocks
    private IngredientController controller;

    private Ingredient cheese;

    @BeforeEach
    void setUp() {
        cheese = new Ingredient();
        cheese.setId(1L);
        cheese.setName("Cheese");
        cheese.setPrice(5.0);
        cheese.setInventory(100);
        cheese.setAddOn(true);
    }

    // ── GET /api/ingredients ─────────────────────────────────────────────────

    @Test
    void getIngredients_returnsOk_andList() {
        when(service.getIngredients()).thenReturn(List.of(cheese));

        ResponseEntity<List<Ingredient>> response = controller.getIngredients();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Cheese", response.getBody().get(0).getName());
    }

    @Test
    void getIngredients_returnsEmptyList_whenNoIngredients() {
        when(service.getIngredients()).thenReturn(List.of());

        ResponseEntity<List<Ingredient>> response = controller.getIngredients();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ── GET /api/ingredients/{id} ────────────────────────────────────────────

    @Test
    void getIngredient_returnsOk_whenFound() {
        when(service.getIngredient(1L)).thenReturn(cheese);

        ResponseEntity<Ingredient> response = controller.getIngredient(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cheese", response.getBody().getName());
        assertEquals(5.0, response.getBody().getPrice());
    }

    @Test
    void getIngredient_throwsException_whenNotFound() {
        when(service.getIngredient(999L)).thenThrow(new RuntimeException("Ingrediens ikke fundet: 999"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.getIngredient(999L));

        assertEquals("Ingrediens ikke fundet: 999", ex.getMessage());
    }

    // ── POST /api/ingredients ────────────────────────────────────────────────

    @Test
    void createIngredient_returnsCreated() {
        when(service.createIngredient(any(Ingredient.class))).thenReturn(cheese);

        ResponseEntity<Ingredient> response = controller.createIngredient(cheese);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cheese", response.getBody().getName());
    }

    @Test
    void createIngredient_callsService() {
        when(service.createIngredient(any(Ingredient.class))).thenReturn(cheese);

        controller.createIngredient(cheese);

        verify(service, times(1)).createIngredient(any(Ingredient.class));
    }

    @Test
    void createIngredient_returnsCorrectFields() {
        Ingredient bacon = new Ingredient();
        bacon.setId(2L);
        bacon.setName("Bacon");
        bacon.setPrice(8.0);
        bacon.setInventory(50);
        bacon.setAddOn(true);

        when(service.createIngredient(any(Ingredient.class))).thenReturn(bacon);

        ResponseEntity<Ingredient> response = controller.createIngredient(bacon);

        assertEquals("Bacon", response.getBody().getName());
        assertEquals(8.0, response.getBody().getPrice());
        assertEquals(50, response.getBody().getInventory());
    }

    // ── PUT /api/ingredients/{id} ────────────────────────────────────────────

    @Test
    void updateIngredient_returnsOk_andUpdatedIngredient() {
        Ingredient updated = new Ingredient();
        updated.setId(1L);
        updated.setName("Extra Cheese");
        updated.setPrice(7.0);
        updated.setInventory(80);
        updated.setAddOn(true);

        when(service.updateIngredient(eq(1L), any(Ingredient.class))).thenReturn(updated);

        ResponseEntity<Ingredient> response = controller.getIngredient(1L, updated);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Extra Cheese", response.getBody().getName());
        assertEquals(7.0, response.getBody().getPrice());
        assertEquals(80, response.getBody().getInventory());
    }

    @Test
    void updateIngredient_throwsException_whenNotFound() {
        when(service.updateIngredient(eq(999L), any(Ingredient.class)))
                .thenThrow(new RuntimeException("Ingrediens ikke fundet: 999"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.getIngredient(999L, cheese));

        assertEquals("Ingrediens ikke fundet: 999", ex.getMessage());
    }

    @Test
    void updateIngredient_callsService() {
        when(service.updateIngredient(eq(1L), any(Ingredient.class))).thenReturn(cheese);

        controller.getIngredient(1L, cheese);

        verify(service, times(1)).updateIngredient(eq(1L), any(Ingredient.class));
    }

    // ── DELETE /api/ingredients/{id} ─────────────────────────────────────────

    @Test
    void deleteIngredient_returnsNoContent() {
        doNothing().when(service).deleteIngredient(1L);

        ResponseEntity<Void> response = controller.deleteIngredient(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteIngredient_callsService() {
        doNothing().when(service).deleteIngredient(1L);

        controller.deleteIngredient(1L);

        verify(service, times(1)).deleteIngredient(1L);
    }

    @Test
    void deleteIngredient_throwsException_whenNotFound() {
        doThrow(new RuntimeException("Ingrediens ikke fundet: 999"))
                .when(service).deleteIngredient(999L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.deleteIngredient(999L));

        assertEquals("Ingrediens ikke fundet: 999", ex.getMessage());
    }
}

