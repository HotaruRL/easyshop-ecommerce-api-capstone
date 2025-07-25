package org.yearup.data;

import org.yearup.models.Order;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;

public interface OrderDao {
    Order add(int userId, Profile profile, ShoppingCart shoppingCart);
    Order getById(int orderId);
}
