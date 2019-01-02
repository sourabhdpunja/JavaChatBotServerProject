package edu.northeastern.ccs.im.message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Sourabh Punja on 12/1/2018
 */
public interface BaseDao {

    /**
     * Sets connection.
     *
     * @param connection the connection
     */
    void setConnection(final Connection connection);

    /**
     * Sets statement.
     *
     * @param statement the statement
     */
    void setStatement(final PreparedStatement statement);

    /**
     * Sets Result Set.
     *
     * @param rs the Result set
     */
    void setRs(final ResultSet rs);
}
