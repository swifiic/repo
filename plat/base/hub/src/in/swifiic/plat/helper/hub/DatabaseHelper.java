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
		String filePath = " Not Set ";
		String driverClass = " Not Set ";
		String dbUrl = " Not Set ";
		String userName = " Not Set ";
		String password = " Not Set ";
		try {
			Properties dbProperties=new Properties();
			String base = System.getenv("SWIFIIC_HUB_BASE");
			if(null != base) {
				filePath = base + "/properties/";
			} else {
				System.err.println("SWIFIIC_HUB_BASE not set");
			}
			FileInputStream fis = new FileInputStream(filePath + "dbConnection.properties");
			dbProperties.load(fis);
			driverClass=dbProperties.getProperty("dbDriver");
			dbUrl=dbProperties.getProperty("dbUrl");
			userName=dbProperties.getProperty("dbUserName");
			password=dbProperties.getProperty("dbPassword");
			// Registering JDBC Driver
			Class.forName(driverClass);
			// Opens a connection to the database
			connection = DriverManager.getConnection(dbUrl, userName, password);
			return connection;
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found " + e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("SQL exception " + e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("File not found " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO exception " + e.getMessage());
			e.printStackTrace();
		}
		if(null == connection) {
			System.err.println("Error - values of connection : filePath driverClass dbUrl userName password  are " +
						filePath + ", " + driverClass + ", " +  dbUrl + ", " +  userName + ", " +  password);
			
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
