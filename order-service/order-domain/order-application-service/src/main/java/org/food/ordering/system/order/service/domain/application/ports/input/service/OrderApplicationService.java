package org.food.ordering.system.order.service.domain.application.ports.input.service;

import jakarta.validation.Valid;
import org.food.ordering.system.order.service.domain.application.dto.create.CreateOrderCommand;
import org.food.ordering.system.order.service.domain.application.dto.create.CreateOrderResponse;
import org.food.ordering.system.order.service.domain.application.dto.track.TrackOrderQuery;
import org.food.ordering.system.order.service.domain.application.dto.track.TrackOrderResponse;

public interface OrderApplicationService {
    CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand);
    TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery);
}
