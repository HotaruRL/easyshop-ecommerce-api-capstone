package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    private MySqlProductDao mySqlProductDao;

    public MySqlShoppingCartDao(DataSource dataSource, MySqlProductDao mySqlProductDao) {
        super(dataSource);
        this.mySqlProductDao = mySqlProductDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart shoppingCart = new ShoppingCart();

        String sql = "SELECT * FROM shopping_cart AS sc " +
                "WHERE user_id = ?;";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet row = statement.executeQuery();

            while (row.next()) {
                ShoppingCartItem shoppingCartItem = mapRow(row);
                shoppingCart.add(shoppingCartItem);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return shoppingCart;
    }

    private ShoppingCartItem mapRow(ResultSet row) throws SQLException {
        int productId = row.getInt("product_id");
        int quantity = row.getInt("quantity");

        Product product = mySqlProductDao.getById(productId);

        ShoppingCartItem shoppingCartItem = new ShoppingCartItem() {{
            setProduct(product);
            setQuantity(quantity);
        }};

        return shoppingCartItem;
    }
}
