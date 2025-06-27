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

        String sql = "SELECT * FROM shopping_cart sc " +
                "JOIN products p ON sc.product_id = p.product_id " +
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
        ShoppingCartItem existingItem = getCartItem(userId, id);

        String sql;

        if (existingItem != null) {
            sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?;";
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, existingItem.getQuantity() + 1);
                statement.setInt(2, userId);
                statement.setInt(3, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1);";
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setInt(2, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return getByUserId(userId);
    }

    @Override
    public ShoppingCart update(int userId, int productId, QuantityDto quantityDto) {
        int newQuantity = quantityDto.getQuantity();

        if (newQuantity <= 0) {
            String sql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?;";
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setInt(2, productId);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            ShoppingCartItem existingItem = getCartItem(userId, productId);

            if (existingItem != null) {
                String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?;";
                try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, newQuantity);
                    statement.setInt(2, userId);
                    statement.setInt(3, productId);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Item does not exist, so INSERT it.
                String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?);";
                try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setInt(1, userId);
                    statement.setInt(2, productId);
                    statement.setInt(3, newQuantity);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return getByUserId(userId);
    }

    @Override
    public ShoppingCart clear(int userId) {
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
        currentShoppingCart = getByUserId(userId);
        return currentShoppingCart;
    }

    protected ShoppingCartItem mapRow(ResultSet row) throws SQLException {
        ShoppingCartItem item = new ShoppingCartItem();

        Product product = new Product();
        product.setProductId(row.getInt("product_id"));
        product.setName(row.getString("name"));
        product.setPrice(row.getBigDecimal("price"));
        product.setCategoryId(row.getInt("category_id"));
        product.setDescription(row.getString("description"));
        product.setColor(row.getString("color"));
        product.setImageUrl(row.getString("image_url"));
         product.setStock(row.getInt("stock"));
         product.setFeatured(row.getBoolean("featured"));

        item.setProduct(product);
        item.setQuantity(row.getInt("quantity"));

        return item;
    }

    private ShoppingCartItem getCartItem(int userId, int productId) {
        String sql = "SELECT * FROM shopping_cart sc " +
                "JOIN products p ON sc.product_id = p.product_id " +
                "WHERE user_id = ? AND sc.product_id = ?;";

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, productId);

            ResultSet row = statement.executeQuery();

            if (row.next()) {
                return mapRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
