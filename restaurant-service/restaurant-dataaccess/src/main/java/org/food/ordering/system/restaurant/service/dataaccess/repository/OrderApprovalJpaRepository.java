package org.food.ordering.system.restaurant.service.dataaccess.repository;

import org.food.ordering.system.restaurant.service.dataaccess.entity.OrderApprovalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderApprovalJpaRepository extends JpaRepository<OrderApprovalEntity, UUID> {
}
