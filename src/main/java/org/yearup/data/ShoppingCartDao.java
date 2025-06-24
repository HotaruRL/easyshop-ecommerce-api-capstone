package org.yearup.data;

import org.yearup.data.dto.QuantityDto;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);

    ShoppingCart add(int userId, int id);

    ShoppingCart update(int userId, int id, QuantityDto quantity);
}
