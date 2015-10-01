package in.swifiic.plat.helper.hub;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DatabaseHelper {
	
	/*
	 * Opens a connection to MySQL Database and returns the 
	 * connection.
	 */
	
	public static Connection connectToDB() {
		Connection connection = null;
		try {
			Properties dbProperties=new Properties();
			
			dbProperties.load(new FileInputStream("/home/aarthi/swifiic/repo/plat/base/hub/src/dbConnection.properties"));
			String driverClass=dbProperties.getProperty("dbDriver");
			String dbUrl=dbProperties.getProperty("dbUrl");
			String userName=dbProperties.getProperty("dbUserName");
			String password=dbProperties.getProperty("dbPassword");
			// Registering JDBC Driver
			Class.forName(driverClass);
			// Opens a connection to the database
			connection = DriverManager.getConnection(dbUrl, userName, password);
			return connection;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
