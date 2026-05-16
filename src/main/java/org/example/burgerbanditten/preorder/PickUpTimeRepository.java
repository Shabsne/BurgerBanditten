package org.example.burgerbanditten.preorder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PickUpTimeRepository extends JpaRepository<PickUpTime, Long> {
    PickUpTime findByOrderId(Long orderId);
}
