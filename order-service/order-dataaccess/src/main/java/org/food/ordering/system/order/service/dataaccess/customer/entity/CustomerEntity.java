package org.food.ordering.system.order.service.dataaccess.customer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_customers_m_view", schema = "customer")
@Entity
public class CustomerEntity {
    @Id
    private UUID id;
}
