package org.yearup.data.dto;

import org.springframework.stereotype.Component;

@Component
public class QuantityDto {
    private int quantity;

    public QuantityDto() {
    }

    public QuantityDto(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
