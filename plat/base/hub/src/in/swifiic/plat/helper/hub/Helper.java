package in.swifiic.plat.helper.hub;

import in.swifiic.plat.helper.hub.SwifiicHandler.Context;
import in.swifiic.plat.helper.hub.xml.Action;
import in.swifiic.plat.helper.hub.xml.Notification;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.mysql.jdbc.Blob;
import com.sun.org.apache.xml.internal.security.utils.Base64;

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
			sql = "SELECT DtnId FROM User WHERE Name=\'" + user + "\'";
			result = statement.executeQuery(sql);
			// Extract data from result set
			while(result.next()) {
				// Retrieve by column name
				String dtnId = result.getString("DtnId");
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
			sql = "SELECT DtnId FROM User";
			result = statement.executeQuery(sql);
			// Extract data from result set
			while(result.next()) {
				// Retrieve by column name
				String dtnId = result.getString("DtnId");
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
		byte[] imageBytes;
		ResultSet result;
		try {
			statement = connection.createStatement();
			sql = "SELECT Name,Alias,ProfilePic FROM User";
			result = statement.executeQuery(sql);
			// Extract data from result set
			while(result.next()) {
				// Retrieve by column name
				username = result.getString("Name");
				alias = result.getString("Alias");
				imageBlob = (Blob) result.getBlob("ProfilePic");
				imageBytes = imageBlob.getBytes(1, (int) imageBlob.length());
				imageEncoded64 = Base64.encode(imageBytes);
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
	
	 /**
	  * This function is added for billing purpose whenever a message is sent from swifiic messenger.
	  * It is taking the UserName as parameter and inserting an entry in OperatorLedger Table.
	  * @param fromUser
	  * As of now we are assuming UserID=1 is always Operator. So, the Credit USerID is 1
	  * Request user id is the user id of the person who is sending message
	  * Request device id we are putting as 0 ----/#temporarily.
	  * amount also 1.
	  * @return
	  */
	public static boolean debitUser(String fromUser){
        Connection con=DatabaseHelper.connectToDB();
        if(con!=null)
        	System.out.println("Connection Successful");
      
		PreparedStatement stmt=null;
		ResultSet rs=null;
	    PreparedStatement pst=null;
		int userId=0;
		Boolean res;
		try {
			stmt=con.prepareStatement("select UserId from User where Name=?");
			stmt.setString(1, fromUser);
			rs=stmt.executeQuery();
			if(rs.next())
			{
				userId=rs.getInt("UserId");
			}
			pst=con.prepareStatement("insert into OperatorLedger(EventNotes,ReqUserId,ReqDeviceId,Details,CreditUserId,DebitUserId,Amount)values(?,?,?,?,?,?,?)");
			pst.setString(1, "Debit For Message");
			pst.setInt(2,userId);
			pst.setInt(3, 0);
			pst.setString(4, "Debit");
			pst.setInt(5,1);
			pst.setInt(6,userId);
			pst.setInt(7,1);
			res=pst.execute();
			if(pst.getUpdateCount()==1){
				System.out.println("Inserted Successfully");
				res = true;
			}
			rs.close();
			stmt.close();
			pst.close();
			DatabaseHelper.closeDB(con);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			res=false;
			e.printStackTrace();
		}
		return res;
	
	}
}
