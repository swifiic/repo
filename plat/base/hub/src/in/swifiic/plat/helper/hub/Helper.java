package in.swifiic.plat.helper.hub;

import in.swifiic.plat.helper.hub.SwifiicHandler.Context;
import in.swifiic.plat.helper.hub.xml.Action;
import in.swifiic.plat.helper.hub.xml.Notification;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.lang.String;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.mysql.jdbc.Blob;

import ibrdtn.api.Base64;

public class Helper {
	private static final String TAG = "Helper";

	public static Properties sqlProperties=null;
	static
	{
	String filePath = "not set";
	try
	{
	String base = System.getenv("SWIFIIC_HUB_BASE");
	if(null != base) {
		filePath = base + "/properties/";
	} else {
		System.err.println("SWIFIIC_HUB_BASE not set");
	}
	FileInputStream fis = new FileInputStream(filePath + "sqlQueries.properties");
        sqlProperties=new Properties();
	sqlProperties.load(fis);
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	if(null == sqlProperties) {
		System.err.println("Error - in loading SQL properties for query string. Filepath was :" + filePath );
	}
	}

	private static final Logger logger = LogManager.getLogManager().getLogger("");


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
		PreparedStatement statement;
		String sql=sqlProperties.getProperty("user.findDtnId");
		ResultSet result;
		try {
			statement = connection.prepareStatement(sql);
			statement.setString(1, user);
			result = statement.executeQuery();
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
		PreparedStatement statement;
		String sql=sqlProperties.getProperty("user.findAllDtnIds");
		ResultSet result;
		try {
			statement = connection.prepareStatement(sql);
			result = statement.executeQuery();
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
	
	/***
	 * @author aarthi
	 * Format username|alias;username|alias;...
	 * Sends the list of users to suta andi devices
	 * User list is sorted in descending order on the basis of time of last update from app.
	 */
	public static String getAllUsers() {
		String users = "";
		Connection connection = DatabaseHelper.connectToDB();
		PreparedStatement statement;
		String sql=sqlProperties.getProperty("user.retriveUsers");
		String username, alias, imageEncoded64;
		Blob imageBlob;
		byte[] imageBytes;
		ResultSet result;
		try {
			statement = connection.prepareStatement(sql);
			result = statement.executeQuery();
			// Extract data from result set
			while(result.next()) {
				// Retrieve by column name
				username = result.getString("Name");
				alias = result.getString("Alias");
				imageBlob = (Blob) result.getBlob("ProfilePic");
				imageBytes = imageBlob.getBytes(1, (int) imageBlob.length());
				imageEncoded64 = Base64.encodeBytes(imageBytes);
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




	/***
	 *getting account details fo all users
	 * totalaccountdetails = accountDetails||accountDetails..;
	 * where accountDetails is for single user and is computed in another function
	 * @return
	 */

	public static String getAccountDetailsForAll(){
		String totalAccountDetails = "";

		Connection connection = DatabaseHelper.connectToDB();
		PreparedStatement statement;
		String userMacIdSql = sqlProperties.getProperty("user.retriveUserMacId");
		ResultSet result;
		String macAddress;
		try {
			statement = connection.prepareStatement(userMacIdSql);
			result = statement.executeQuery();
			while(result.next()) {
				macAddress = result.getString("MacAddress");
				if ( macAddress == null ||  macAddress.equals("00:00:00:00:00:00")){
					continue;
				}
				totalAccountDetails += getAccountDetails(macAddress)+"||";
			}
			result.close();
			statement.close();
			DatabaseHelper.closeDB(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return totalAccountDetails;
	}




	/***
	getting account details for specific user,users are obtained from getAllUers()
	 accountDetails = macAddress|remainingcredit|credit1,amount1,,credit2,amount2,,
	*/

	private static String getAccountDetails(String macAddress){
		// transactions are Details,Amount
		// here number of transaction details are limited by a constant in sqlqueries.properties
		// account details contains remaining credit and transaction dtails

		String accountDetails="";
		String transactionDetails="";

		int userid=0;
		int remainingCredit=0;
		int amount;
		String details;

		Connection connection = DatabaseHelper.connectToDB();
		PreparedStatement statement1,statement2;

		//String useridsql = sqlProperties.getProperty("user.retriveUserId")
		//String remainingcreditsql = sqlProperties.getProperty("user.retriveCredit");
		String idAndCreditsql = sqlProperties.getProperty("user.retriveIdAndCredit");
		String transactionDetailsSql = sqlProperties.getProperty("opertorLedger.retriveTransactionDetails");

		ResultSet result1;
		ResultSet result2;

		// retreving userid and credit
		try {
			// here one user name should have only one userid
			statement1 = connection.prepareStatement(idAndCreditsql);
			statement1.setString(1, macAddress);
			result1 = statement1.executeQuery();
			userid = result1.getInt("UserId");
			remainingCredit = result1.getInt("RemainingCreditPostAudit");
			result1.close();
			statement1.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		accountDetails = macAddress+"|"+remainingCredit+"|";

		// retreving transactions

		try {
			statement2 = connection.prepareStatement(transactionDetailsSql);
			statement2.setInt(1, userid);
			result2 = statement2.executeQuery();
			while(result2.next()) {
				// Retrieve by column name
				amount = result2.getInt("Amount");
				details = result2.getString("Details");
				transactionDetails += amount+","+details+",,";
			}
			result2.close();
			statement2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}


		DatabaseHelper.closeDB(connection);

		accountDetails += transactionDetails;

		return  accountDetails;
	}
	
	 /**
	  * @author aarthi 
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
		String query=sqlProperties.getProperty("user.findUserId");
		String insertQuery=sqlProperties.getProperty("opertorLedger.insert");
		
		Boolean res;
		try {
			stmt=con.prepareStatement(query);
			stmt.setString(1, fromUser);
			rs=stmt.executeQuery();
			if(rs.next())
			{
				userId=rs.getInt("UserId");
			}
			pst=con.prepareStatement(insertQuery);
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
	/***
	 * @author aarthi
	 * This function updates the MacAddress and DTN ID of the user devices in User Table
	 * @param macId - MacAddress of the device
	 * @param time  -Time of last update from Suta app
	 * @param dtnId - DTN ID of the user device
	 * @param fromUser - Name of User
	 */
	public static void updateDatabase(String macId,String time,String dtnId,String fromUser)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // your template here
		
		String dtn_id=null,mac_address=null;
		 Connection con=DatabaseHelper.connectToDB();
	        if(con!=null)
	        logger.log(Level.INFO,"Connection Successful to DB");
	        PreparedStatement stmt=null;
	        PreparedStatement pst=null;
			ResultSet rs=null;
			String selectQuery=sqlProperties.getProperty("user.findMacAddress");
			String updateQuery1=sqlProperties.getProperty("user.updateDtnIdMacAddress");
			String updateQuery2=sqlProperties.getProperty("user.updateDtnId");
			String updateQuery3=sqlProperties.getProperty("user.updateTimeOfLastUpdate");
			try {
				java.util.Date dateStr = formatter.parse(time);
				java.sql.Date dateDB = new java.sql.Date(dateStr.getTime());
				stmt=con.prepareStatement(selectQuery);
				stmt.setString(1, fromUser);
				rs=stmt.executeQuery();
				if(rs.next())
				{
					dtn_id=rs.getString("DtnId");
					mac_address=rs.getString("MacAddress");
				}
				//if MAC Address of the device is not initialized. update macaddress and dtn id both
				if(mac_address.equals("00:00:00:00:00:00"))
				{
					pst=con.prepareStatement(updateQuery1);
					pst.setString(1, dtnId);
					pst.setString(2,macId);
					pst.setTimestamp(3,new java.sql.Timestamp(dateStr.getTime()));
					pst.setString(4,fromUser);
					pst.execute();
					return;
				}
				//if macaddress is initialized and dtn id changes allow update
				else if(mac_address.equals(macId)&& !(dtn_id.equals(dtnId)))
				{
					pst=con.prepareStatement(updateQuery2);
					pst.setString(1, dtnId);
					pst.setTimestamp(2,new java.sql.Timestamp(dateStr.getTime()));
					pst.setString(3,fromUser);
					pst.execute();
					return;
				}
				//dtnid matches and mac_address differs , throw error
				else if(!(mac_address.equals(macId))&& dtn_id.equals(dtnId))
				{
					logger.log(Level.SEVERE,"Mac Address already initalized for this dtn id ");
					return;
				}
				//both matches allow update of time
				else if(mac_address.equals(macId)&& (dtn_id.equals(dtnId)))
				{
					pst=con.prepareStatement(updateQuery3);
					pst.setTimestamp(1,new java.sql.Timestamp(dateStr.getTime()));
					pst.setString(2, fromUser);
					pst.execute();
					return;
				}
						
			} 
			catch (SQLException e) {
			logger.log(Level.SEVERE,e.toString());
				//e.printStackTrace();
			}
			catch(Exception e)
			{
				logger.log(Level.SEVERE,e.toString());
			}
	}
	
}
