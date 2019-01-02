package edu.northeastern.ccs.im.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connect to Database
 * @author Sourabh Punja on 11/30/2018
 */
public class ConnectionFactory {

    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://prattledb.cfmiqlmluzk4.us-east-1.rds.amazonaws.com/team212";
    static final String USERNAME = "team212";
    static final String CREDENTIALS = "dunkinhot";
    private static final String SQL_MESSAGE_EXCEPTION = "SQL Exception";
    private static final String CLASS_NOT_FOUND_EXCEPTION = "Class Not Found Exception";
    private static Logger logger = LogManager.getLogger(ConnectionFactory.class);

    private ConnectionFactory() {
    }

    /**
     * Get a connection to database
     * @return Connection object
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL,USERNAME,CREDENTIALS);
        } catch (SQLException ex) {
            logger.error(SQL_MESSAGE_EXCEPTION);
        } catch (ClassNotFoundException ex) {
            logger.error(CLASS_NOT_FOUND_EXCEPTION);
        }
        return connection;
    }
}