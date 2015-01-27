package in.swifiic.plat.helper.hub;

import in.swifiic.plat.helper.hub.SwifiicHandler.Context;
import in.swifiic.plat.helper.hub.xml.Action;
import in.swifiic.plat.helper.hub.xml.Notification;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.mysql.jdbc.Blob;
import org.apache.commons.codec.binary.Base64;
//import com.sun.org.apache.xml.internal.security.utils.Base64;

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
		String username, alias, imageEncoded64;
		Blob imageBlob;
		ResultSet result;
		try {
			statement = connection.createStatement();
			sql = "SELECT username,alias,profile_pic FROM Users";
			result = statement.executeQuery(sql);
			// Extract data from result set
			while(result.next()) {
				// Retrieve by column name
				username = result.getString("username");
				alias = result.getString("alias");
				imageBlob = (Blob) result.getBlob("profile_pic");
				byte[] imageBytes = imageBlob.getBytes(1, (int) imageBlob.length());
				imageEncoded64 = Base64.encodeBase64String(imageBytes);
				users += username + "|" + alias + "|" + imageEncoded64 + ";";
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
