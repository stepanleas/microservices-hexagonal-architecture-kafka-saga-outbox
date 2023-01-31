package org.food.ordering.system.restaurant.service.dataaccess.adapter;

import lombok.RequiredArgsConstructor;
import org.food.ordering.system.restaurant.service.dataaccess.mapper.RestaurantDataAccessMapper;
import org.food.ordering.system.restaurant.service.dataaccess.repository.OrderApprovalJpaRepository;
import org.food.ordering.system.restaurant.service.domain.core.entity.OrderApproval;
import org.food.ordering.system.restaurant.service.domain.application.ports.output.repository.OrderApprovalRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderApprovalRepositoryImpl implements OrderApprovalRepository {

    private final OrderApprovalJpaRepository orderApprovalJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    @Override
    public OrderApproval save(OrderApproval orderApproval) {
        return restaurantDataAccessMapper
            .orderApprovalEntityToOrderApproval(orderApprovalJpaRepository
                .save(restaurantDataAccessMapper.orderApprovalToOrderApprovalEntity(orderApproval)));
    }
}
