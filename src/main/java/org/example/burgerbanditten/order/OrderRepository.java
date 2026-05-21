package org.example.burgerbanditten.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @Query("""
            select o from Order o
            where o.orderStatus in :statuses
            and (:from is null or o.createdAt >= :from)
            and (:to is null or o.createdAt < :to)
            """)
    List<Order> findSalesOrders(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
