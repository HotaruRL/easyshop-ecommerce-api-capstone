package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderLineItemDao;

import javax.sql.DataSource;

@Component
public class MySqlOrderLineItemDao extends MySqlDaoBase implements OrderLineItemDao {
    public MySqlOrderLineItemDao(DataSource dataSource) {
        super(dataSource);
    }
}
