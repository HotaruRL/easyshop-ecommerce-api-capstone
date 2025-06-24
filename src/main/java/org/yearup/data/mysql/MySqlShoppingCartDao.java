package org.yearup.data.mysql;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.dto.QuantityDto;
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

        String sql = "SELECT * FROM shopping_cart " +
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

    @Override
    public ShoppingCart update(int userId, int id, QuantityDto quantityDto) {
        ShoppingCart currentShoppingCart = getByUserId(userId);

        if (!currentShoppingCart.contains(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            int quantity = quantityDto.getQuantity();

            String sql = "UPDATE shopping_cart SET quantity = ? " +
                    "WHERE user_id = ? AND product_id = ?";

            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, quantity);
                statement.setInt(2, userId);
                statement.setInt(3, id);

                statement.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        currentShoppingCart = getByUserId(userId);
        return currentShoppingCart;
    }

    @Override
    public void clear(int userId) {
        ShoppingCart currentShoppingCart = getByUserId(userId);
        String sql = "DELETE FROM shopping_cart " +
                "WHERE user_id = ?;";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
