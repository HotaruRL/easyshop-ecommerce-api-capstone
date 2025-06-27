package org.yearup.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCart {
    private List<ShoppingCartItem> items = new ArrayList<>();

    public List<ShoppingCartItem> getItems() {
        return items;
    }

    public void setItems(List<ShoppingCartItem> items) {
        this.items = items;
    }

    public void add(ShoppingCartItem item) {
        items.add(item);
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(ShoppingCartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
