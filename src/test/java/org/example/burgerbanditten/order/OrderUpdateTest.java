package org.example.burgerbanditten.order;

import org.example.burgerbanditten.email.EmailService;
import org.example.burgerbanditten.order.dto.UpdateOrderRequest;
import org.example.burgerbanditten.preorder.PreOrderService;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUpdateTest {

    @Mock private OrderRepository orderRepository;
    @Mock private EmailService emailService;
    @Mock private PreOrderService preOrderService;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Cheese Burger");
        testProduct.setPrice(85.0);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderStatus(OrderStatus.ACCEPTED);
        testOrder.setComment("Original kommentar");
        testOrder.setOrderItems(new ArrayList<>());
        testOrder.setPrice(85.0);
    }

    // ── Administrator kan redigere en aktiv ordre ────────

    @Test
    void skalOpdatereKommentar_NaarAdminRedigerer() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        UpdateOrderRequest request = new UpdateOrderRequest("Ny kommentar", null, null);
        Order result = orderService.updateOrder(1L, request);

        assertEquals("Ny kommentar", result.getComment());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void skalOpdatereVarerOgPris_NaarAdminRedigerer() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any())).thenReturn(testOrder);

        List<UpdateOrderRequest.UpdateOrderItem> items =
                List.of(new UpdateOrderRequest.UpdateOrderItem(1L, 2));
        UpdateOrderRequest request = new UpdateOrderRequest(null, null, items);

        Order result = orderService.updateOrder(1L, request);

        // 2 × 85 kr = 170 kr
        assertEquals(170.0, result.getPrice());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void skalSendeEmail_NaarOrdreHarBruger() {
        org.example.burgerbanditten.user.User user = new org.example.burgerbanditten.user.User();
        user.setMail("kunde@mail.dk");
        user.setName("Test Kunde");
        testOrder.setUser(user);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        UpdateOrderRequest request = new UpdateOrderRequest("Ny kommentar", null, null);
        orderService.updateOrder(1L, request);

        verify(emailService).sendOrderUpdatedNotification("kunde@mail.dk", "Test Kunde", 1L);
    }

    @Test
    void skalIkkeSendeEmail_NaarOrdreErGaest() {
        testOrder.setUser(null);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        UpdateOrderRequest request = new UpdateOrderRequest("Ny kommentar", null, null);
        orderService.updateOrder(1L, request);

        verifyNoInteractions(emailService);
    }

    // ── Ordre kan ikke redigeres efter afslutning ────────

    @Test
    void skalKasteException_NaarOrdreErCompleted() {
        testOrder.setOrderStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        UpdateOrderRequest request = new UpdateOrderRequest("Ny kommentar", null, null);

        assertThrows(IllegalStateException.class, () ->
                orderService.updateOrder(1L, request));
    }

    @Test
    void skalKasteException_NaarOrdreErRejected() {
        testOrder.setOrderStatus(OrderStatus.REJECTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        UpdateOrderRequest request = new UpdateOrderRequest("Ny kommentar", null, null);

        assertThrows(IllegalStateException.class, () ->
                orderService.updateOrder(1L, request));
    }

    @Test
    void skalKasteException_NaarOrdreIkkeFindes() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateOrderRequest request = new UpdateOrderRequest("Ny kommentar", null, null);

        assertThrows(IllegalArgumentException.class, () ->
                orderService.updateOrder(99L, request));
    }
}