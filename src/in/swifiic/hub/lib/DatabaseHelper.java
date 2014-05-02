package in.swifiic.hub.lib;

import java.sql.*;

public class DatabaseHelper {
	// JDBC Driver details and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/swifiic";
	
	// Database credentials
	static final String USER = "swifiic";
	static final String PASS = "why";
	
	/*
	 * Opens a connection to MySQL Database and returns the 
	 * connection.
	 */
	public static Connection connectToDB() {
		Connection connection = null;
		try {
			// Registering JDBC Driver
			Class.forName("com.mysql.jdbc.Driver");
			// Opens a connection to the database
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			return connection;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}
	
	/*
	 * Closes the database connection
	 */
	public static boolean closeDB(Connection link) {
		try {
			link.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
}