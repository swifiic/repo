package in.swifiic.app.msngr.hub;

import ibrdtn.example.api.DTNClient;
import in.swifiic.plat.helper.hub.Base;
import in.swifiic.plat.helper.hub.DatabaseHelper;
import in.swifiic.plat.helper.hub.Helper;
import in.swifiic.plat.helper.hub.SwifiicHandler;
import in.swifiic.plat.helper.hub.xml.Action;
import in.swifiic.plat.helper.hub.xml.Notification;
import in.swifiic.plat.helper.hub.SwifiicLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import ibrdtn.api.ExtendedClient;


public class Messenger extends Base implements SwifiicHandler {

	private static final Logger logger = LogManager.getLogManager().getLogger("");
	private DTNClient dtnClient;


	protected ExecutorService executor = Executors.newCachedThreadPool();

	// Following is the name of the endpoint to register with
	protected static String PRIMARY_EID = "Msngr";
	private static final String logFileName = "msngr_log";
	private static final String errorFileName = "msngr_error";

	public static org.apache.logging.log4j.Logger logNew=org.apache.logging.log4j.LogManager.getLogger("in.swifiic.app.msngr.hub.Messenger");
	public Messenger() {
		super(PRIMARY_EID);
		// Initialize connection to daemon
		dtnClient = getDtnClient(PRIMARY_EID, this);
		// logger.log(Level.INFO, dtnClient.getConfiguration());
		// logNew.info(dtnClient.getConfiguration());
		SwifiicLogger.logMessage(PRIMARY_EID, dtnClient.getConfiguration(), logFileName);

	}

	// fix any exceptions which may occur here
	public static void main(String args[]) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Messenger messenger = new Messenger();
		String input;
		// System.out.print("Enter \"exit\" to exit application: ");
		while(true) {
			// input = br.readLine(); // what if input is null?
			// // while(null == input) {
			// // 	input = br.readLine();
			// // }
			// if(input.equalsIgnoreCase("exit")) {
			// 	messenger.exit();
			// } else {
			// 	continue;
			// }
			ExtendedClient ec = messenger.getDtnClientInstance().getEC(); // does instance need to be received each time?
			if(!ec.isConnected()){
				SwifiicLogger.logMessage(PRIMARY_EID, "Messenger attempting reconnect with the service",
										errorFileName);
				System.err.println("Messenger attempting reconnect with the service");
				messenger.getDtnClientInstance().reconnect();
			}
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted " + e);
				SwifiicLogger.logMessage(PRIMARY_EID, "Thread interrupted " + e,
										errorFileName);
			}
		}
	}

	@Override
		public void handlePayload(String payload, final Context ctx,String srcurl) {
			super.handlePayload(payload, ctx, srcurl);
			final String message = new String(payload); // 2ASK: why are we even doing this?
			System.out.println(srcurl);
			SwifiicLogger.logMessage(PRIMARY_EID, "Payload received:\n" + payload, logFileName); // 2ASK: this is basically logging the same thing twice
			SwifiicLogger.logMessage(PRIMARY_EID, "Message received from " + srcurl +":\n" + message, logFileName);

			// Helper.logHubMessage(PRIMARY_EID, srcurl, deviceDtnId);

			System.err.println("Got Message:" + message);
			System.err.println("Got Payload:" + payload);
			// logNew.info("\n Got Message:" +payload);
			executor.execute(new Runnable() {
				@Override
				public void run() {
				try {
					System.err.println("In run function Message:" + message);
					Action action = Helper.parseAction(message);
					if(null == action) {
						throw new Exception("Failed to parse message:" + message);
					}

					Notification notif = new Notification(action);
					notif.updateNotificatioName("DeliverMessage");

					String toUser = action.getArgument("toUser");
					String fromUser = action.getArgument("fromUser");

					// A user may have multiple devices - deprecated for now - only one device per user
					String deviceDtnId = Helper.getDeviceDtnIdForUser(toUser, ctx);

					String response = Helper.serializeNotification(notif);
					send(deviceDtnId + "/in.swifiic.app.msngr.andi" , response);
					// Mark bundle as delivered...
					// SwifiicLogger.logMessage(PRIMARY_EID, "Unable to process message and
					// 						send response\n" + e.getMessage(), logFileName);
					logger.log(Level.INFO, "Attempted to send to {1}, had received \n{0}\n and responsed with \n {2}",
							new Object[] {message, deviceDtnId + "/in.swifiic.app.msngr.andi", response});
					boolean status = Helper.debitUser(fromUser);
				} catch (Exception e) {
					SwifiicLogger.logMessage(PRIMARY_EID, "Unable to process message and send response\n" + e.getMessage(), errorFileName);
					// logger.log(Level.SEVERE, "Unable to process message and send response\n" + e.getMessage());
					e.printStackTrace();
					// logNew.info("Unable to process message and send response\n" + e.getMessage());
				}
			}
		});
	}
}
