package org.food.ordering.system.restaurant.service.domain.core.entity;

import lombok.Getter;
import org.food.ordering.system.domain.entity.BaseEntity;
import org.food.ordering.system.domain.valueobject.Money;
import org.food.ordering.system.domain.valueobject.ProductId;

@Getter
public class Product extends BaseEntity<ProductId> {
    private String name;
    private Money price;
    private final int quantity;
    private boolean available;

    public void updateWithConfirmedPriceAndAvailability(String name, Money price, boolean available) {
        this.name = name;
        this.price = price;
        this.available = available;
    }

    private Product(Builder builder) {
        setId(builder.productId);
        name = builder.name;
        price = builder.price;
        quantity = builder.quantity;
        available = builder.available;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ProductId productId;
        private String name;
        private Money price;
        private int quantity;
        private boolean available;

        private Builder() {
        }

        public Builder productId(ProductId val) {
            productId = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder quantity(int val) {
            quantity = val;
            return this;
        }

        public Builder available(boolean val) {
            available = val;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
