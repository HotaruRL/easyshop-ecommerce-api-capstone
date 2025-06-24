package org.yearup.data.dto;

import org.springframework.stereotype.Component;

@Component
public class QuantityDto {
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
