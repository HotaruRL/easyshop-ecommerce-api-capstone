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

    @Override
    public ShoppingCart add(int userId, int id) {
        ShoppingCart currentShoppingCart = getByUserId(userId);

        if (currentShoppingCart.contains(id)) {
            String sql = "UPDATE shopping_cart SET quantity = quantity + 1 " +
                    "WHERE user_id = ? AND product_id = ?";

            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, userId);
                statement.setInt(2, id);

                statement.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            String sql = "INSERT INTO shopping_cart(user_id, product_id, quantity) " +
                    " VALUES (?, ?, 1);";

            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, userId);
                statement.setInt(2, id);

                statement.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        currentShoppingCart = getByUserId(userId);
        return currentShoppingCart;
    }

    protected ShoppingCartItem mapRow(ResultSet row) throws SQLException {
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
