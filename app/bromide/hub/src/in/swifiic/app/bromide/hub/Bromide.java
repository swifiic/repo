package in.swifiic.app.bromide.hub;

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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.Date;
// import java.util.Base64;

import ibrdtn.api.Base64;
import java.io.FileOutputStream;



import ibrdtn.api.ExtendedClient;


public class Bromide extends Base implements SwifiicHandler {
	private int i = 0;
	private DTNClient dtnClient;

	private static final String logDirPath = "/home/nic/logfolder/";
	protected ExecutorService executor = Executors.newCachedThreadPool();

	// Following is the name of the endpoint to register with
	protected static String PRIMARY_EID = "Bromide";
	private static final String logFileName = "bromide_log";
	private static final String errorFileName = "bromide_error";

	public Bromide() {
		super(PRIMARY_EID);
		// Initialize connection to daemon
		dtnClient = getDtnClient(PRIMARY_EID, this);
		SwifiicLogger.logMessage(PRIMARY_EID, dtnClient.getConfiguration(), logFileName);
	}

	// fix any exceptions which may occur here
	public static void main(String args[]) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Bromide messenger = new Bromide();
		String input;
		// System.out.print("Enter \"exit\" to exit application: ");
		while(true) {
			ExtendedClient ec = messenger.getDtnClientInstance().getEC(); // does instance need to be received each time?
			if(!ec.isConnected()){
				SwifiicLogger.logMessage(PRIMARY_EID, "Bromide attempting reconnect with the service",
										errorFileName);
				System.err.println("Bromide attempting reconnect with the service");
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
	public void handlePayload(String payload, final Context ctx, String srcurl) {
		super.handlePayload(payload, ctx, srcurl);
		final String message = new String(payload); // 2ASK: why are we even doing this?
		System.out.println(srcurl);
		SwifiicLogger.logMessage(PRIMARY_EID, "Message received from " + srcurl +":\n" + message, logFileName);

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
					String encodedImage = action.getArgument("encodedImage");
					String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
					FileOutputStream fos = new FileOutputStream(logDirPath+"IMG_"+timeStamp+".jpg", false); //overwriting the file for testing
					try {
						byte[] decodedImage = Base64.decode(encodedImage);
						fos.write(decodedImage);
						SwifiicLogger.logMessage(PRIMARY_EID, "Image saved", logFileName);
					} catch (IllegalArgumentException e) {
						SwifiicLogger.logMessage(PRIMARY_EID, "encodedImage is not valid base64! " + encodedImage, errorFileName);
					} finally {
						fos.close();
					}
				} catch (Exception e) {
					SwifiicLogger.logMessage(PRIMARY_EID, "Unable to process message and send response\n" + e.getMessage(), errorFileName);
					e.printStackTrace();
				}
			}
		});
	}
}
