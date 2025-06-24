package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Product;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao {
    public MySqlProfileDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile) {
        String sql = "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();

            return profile;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile getByUserId(int userId) {

        String sql = "SELECT * FROM profiles WHERE user_id = ?;";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet row = statement.executeQuery();

            if (row.next()) {
                return mapRow(row);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Profile update(int userId, Profile profile) {

        /*
        To handle omitted field:
        COALESCE function will use the first non-null value in the parameter list.
        Effectively, if any value is omitted, it will use the original value in this case

        cleanEmptyData is to make empty String (omitted field) into null so that COALESCE can work properly
         */
        String sql = "UPDATE profiles " +
                "SET first_name = COALESCE(?, first_name) " +
                "  , last_name = COALESCE(?, last_name) " +
                "  , phone = COALESCE(?, phone) " +
                "  , email = COALESCE(?, email) " +
                "  , address = COALESCE(?, address) " +
                "  , city = COALESCE(?, city) " +
                "  , state = COALESCE(?, state) " +
                "  , zip = COALESCE(?, zip) " +
                "WHERE user_id = ?;";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, cleanEmptyData(profile.getFirstName()));
            statement.setString(2, cleanEmptyData(profile.getLastName()));
            statement.setString(3, cleanEmptyData(profile.getPhone()));
            statement.setString(4, cleanEmptyData(profile.getEmail()));
            statement.setString(5, cleanEmptyData(profile.getAddress()));
            statement.setString(6, cleanEmptyData(profile.getCity()));
            statement.setString(7, cleanEmptyData(profile.getState()));
            statement.setString(8, cleanEmptyData(profile.getZip()));
            statement.setInt(9, userId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                // get the newly updated profile
                return getByUserId(userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    protected Profile mapRow(ResultSet row) throws SQLException {
        int userId = row.getInt("user_id");
        String firstName = row.getString("first_name");
        String lastName = row.getString("last_name");
        String phone = row.getString("phone");
        String email = row.getString("email");
        String address = row.getString("address");
        String city = row.getString("city");
        String state = row.getString("state");
        String zip = row.getString("zip");

        return new Profile(userId, firstName, lastName, phone, email, address, city, state, zip);
    }

    protected static String cleanEmptyData(String input) {
        if (input != null && input.isEmpty()) {
            return null;
        }
        return input;
    }

}
