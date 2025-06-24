package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.*;
import org.yearup.services.ShippingService;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {
    private ShippingService shippingService;

    @Autowired
    public MySqlOrderDao(DataSource dataSource, ShippingService shippingService) {
        super(dataSource);
        this.shippingService = shippingService;
    }

    @Override
    public Order add(int userId, Profile profile, ShoppingCart shoppingCart) {
        Connection connection = null;
        Order order = new Order();

        // use transaction so that INSERT for Order and OrderLineItems pass or fail together, prevent partial order
        try {
            connection = getConnection();
            // start the transaction, disable autocommit to rollback in case something goes wrong
            connection.setAutoCommit(false);

            BigDecimal shippingAmount = shippingService.getCheapestShippingRate(profile);

            String sql = "INSERT INTO orders(user_id, date, address, city, state, zip, shipping_amount ) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);";

            try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, userId);
                statement.setObject(2, LocalDateTime.now());
                statement.setString(3, profile.getAddress());
                statement.setString(4, profile.getCity());
                statement.setString(5, profile.getState());
                statement.setString(6, profile.getZip());
                statement.setBigDecimal(7, shippingAmount);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    // Retrieve the generated keys
                    ResultSet generatedKeys = statement.getGeneratedKeys();

                    if (generatedKeys.next()) {
                        // Retrieve the auto-incremented ID
                        int orderId = generatedKeys.getInt(1);

                        // build order object in memory
                        order.setOrderId(orderId);
                        order.setUserId(userId);
                        order.setDate(LocalDateTime.now());
                        order.setAddress(profile.getAddress());
                        order.setCity(profile.getCity());
                        order.setState(profile.getState());
                        order.setZip(profile.getZip());
                        order.setShippingAmount(shippingAmount);
                    } else {
                        throw new SQLException("Creating order failed.");
                    }
                }

                // start adding line items
                addOrderLineItems(connection, order.getOrderId(), shoppingCart);

                // build line items for order object in memory
                for (ShoppingCartItem cartItem : shoppingCart.getItems().values()) {
                    OrderLineItem lineItem = new OrderLineItem();
                    lineItem.setProductId(cartItem.getProductId());
                    lineItem.setQuantity(cartItem.getQuantity());
                    lineItem.setSalePrice(cartItem.getSalesPrice());
                    lineItem.setDiscount(cartItem.getDiscount());
                    order.getLineItems().add(lineItem);
                }

                // make the change permanent to the database
                connection.commit();

                return order; //order object in memory
            }
        } catch (Exception e) {
            // rollback if there's an error
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (Exception ex) {
                    System.err.println("Rollback error:" + ex.getMessage());
                }
            }
            throw new RuntimeException("Transaction failed:" + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public Order getById(int orderId) {
        Order order = null;
        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement1 = connection.prepareStatement(sql);
            statement1.setInt(1, orderId);

            ResultSet row = statement1.executeQuery();

            if (row.next()) {
                order = mapRowToOrder(row);
            } else {
                return null;
            }

            // if the order exists, get its line items
            if (order != null) {
                String lineItemSql = "SELECT * FROM order_line_items WHERE order_id = ?";

                try (PreparedStatement statement2 = connection.prepareStatement(lineItemSql)) {
                    statement2.setInt(1, orderId);

                    ResultSet rs = statement2.executeQuery();

                    while (rs.next()) {
                        OrderLineItem lineItem = mapRowToLineItem(rs);
                        order.getLineItems().add(lineItem);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return order;
    }

    protected static Order mapRowToOrder(ResultSet row) throws SQLException {
        Order order = new Order();
        order.setOrderId(row.getInt("order_id"));
        order.setUserId(row.getInt("user_id"));
        order.setDate(row.getObject("date", LocalDateTime.class));
        order.setAddress(row.getString("address"));
        order.setCity(row.getString("city"));
        order.setState(row.getString("state"));
        order.setZip(row.getString("zip"));
        order.setShippingAmount(row.getBigDecimal("shipping_amount"));
        return order;
    }

    protected static OrderLineItem mapRowToLineItem(ResultSet row) throws SQLException {
        OrderLineItem item = new OrderLineItem();
        item.setOrderLineItemId(row.getInt("order_line_item_id"));
        item.setOrderId(row.getInt("order_id"));
        item.setProductId(row.getInt("product_id"));
        item.setSalePrice(row.getBigDecimal("sales_price"));
        item.setQuantity(row.getInt("quantity"));
        item.setDiscount(row.getBigDecimal("discount"));
        return item;
    }

    protected void addOrderLineItems(Connection connection, int orderId, ShoppingCart shoppingCart) {
        String sql = "INSERT INTO order_line_items(order_id, product_id, sales_price, quantity, discount) " +
                "VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (ShoppingCartItem shoppingCartItem : shoppingCart.getItems().values()) {
                statement.setInt(1, orderId);
                statement.setInt(2, shoppingCartItem.getProductId());
                statement.setBigDecimal(3, shoppingCartItem.getSalesPrice()); // Price at time of sale
                statement.setInt(4, shoppingCartItem.getQuantity());
                statement.setBigDecimal(5, shoppingCartItem.getDiscount());

                //add current statement to a batch for efficiency
                statement.addBatch();
            }

            // execute the whole batch in 1 go
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
