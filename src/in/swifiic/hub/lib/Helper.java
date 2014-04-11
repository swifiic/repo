package in.swifiic.hub.lib;

import in.swifiic.hub.lib.SwifiicHandler.Context;
import in.swifiic.hub.lib.xml.Action;
import in.swifiic.hub.lib.xml.Notification;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class Helper {

	public static Action parseAction(String str) {
        Serializer serializer = new Persister();
        try {
        	Action action = serializer.read(Action.class,str);
        	return action;
        } catch(Exception e) {
        	// This should not happen unless APP tries a random string 
        	// String given from Generic Service was already tested for success
        }
        return null;
	}
	
	public static String serializeNotification(Notification notif) {
        try {
        	 StringWriter writer = new StringWriter();
        	 Serializer serializer = new Persister();  
       	     serializer.write(notif, writer);  

        	 return writer.getBuffer().toString();
        } catch(Exception e) {
        	// This should not happen since Action is a XML serializable object
        }
        return null;
	}

	public static List<String> getDevicesForUser(String user, Context ctx) {
		List<String> deviceList = new ArrayList<String>();
		Connection connection = DatabaseHelper.connectToDB();
		Statement statement;
		String sql;
		ResultSet result;
		try {
			statement = connection.createStatement();
			sql = "SELECT dtn_id FROM Users WHERE username=\'" + user + "\'";
			result = statement.executeQuery(sql);
			// Extract data from result set
			while(result.next()) {
				// Retrieve by column name
				String dtnId = result.getString("dtn_id");
				deviceList.add(dtnId);
			}
			result.close();
			statement.close();
			DatabaseHelper.closeDB(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return deviceList;
	}
	
	public static List<String> getDevicesForAllUsers() {
		List<String> deviceList = new ArrayList<String>();
		Connection connection = DatabaseHelper.connectToDB();
		Statement statement;
		String sql;
		ResultSet result;
		try {
			statement = connection.createStatement();
			sql = "SELECT dtn_id FROM Users";
			result = statement.executeQuery(sql);
			// Extract data from result set
			while(result.next()) {
				// Retrieve by column name
				String dtnId = result.getString("dtn_id");
				deviceList.add(dtnId);
			}
			result.close();
			statement.close();
			DatabaseHelper.closeDB(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return deviceList;
	}
	
	/*
	 * Format username|alias;username|alias;...
	 */
	public static String getAllUsers() {
		String users = "";
		Connection connection = DatabaseHelper.connectToDB();
		Statement statement;
		String sql;
		ResultSet result;
		try {
			statement = connection.createStatement();
			sql = "SELECT username,alias FROM Users";
			result = statement.executeQuery(sql);
			// Extract data from result set
			while(result.next()) {
				// Retrieve by column name
				users += result.getString("username") + "|" + result.getString("alias") + ";";
			}
			result.close();
			statement.close();
			DatabaseHelper.closeDB(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}
}
