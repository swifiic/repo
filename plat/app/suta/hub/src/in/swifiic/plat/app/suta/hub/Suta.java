package in.swifiic.plat.app.suta.hub;

import ibrdtn.example.api.DTNClient;
import in.swifiic.plat.helper.hub.Base;
import in.swifiic.plat.helper.hub.Helper;
import in.swifiic.plat.helper.hub.SwifiicHandler;
import in.swifiic.plat.helper.hub.xml.Notification;
import in.swifiic.plat.helper.hub.SwifiicLogger;

import java.io.*;
import java.lang.String;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import in.swifiic.plat.helper.hub.xml.Action;

import ibrdtn.api.Base64;
import ibrdtn.api.ExtendedClient;

public class Suta extends Base implements SwifiicHandler {
	public static Properties sutaProperties=null;
	static	{
		String filePath = " Not Set ";
		try {
			String base = System.getenv("SWIFIIC_HUB_BASE");
			if(null != base) {
			    filePath = base + "/properties/";
			} else {
			    System.err.println("SWIFIIC_HUB_BASE not set");
			}
			FileInputStream fis = new FileInputStream(filePath + "suta.properties");
			sutaProperties=new Properties();
			sutaProperties.load(fis);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(null == sutaProperties) {
			System.err.println("Error - SUTA Properties not loaded : filePath " + filePath);
		}
	}

	private static final Logger logger = LogManager.getLogManager().getLogger("");
	public static org.apache.logging.log4j.Logger logNew = org.apache.logging.log4j.LogManager.getLogger("in.swifiic.plat.app.suta.hub.Suta");
	private DTNClient dtnClient;
	protected ExecutorService executor = Executors.newCachedThreadPool();
	// Following is the name of the endpoint to register with
	private static final String logDirPath = "/home/nic/logfolder/";
	private static final String logFileName = "suta_log";
	private static final String errorFileName = "suta_error";
	protected static String PRIMARY_EID = "suta";

	public Suta() {
		// Initialize connection to daemon
		super(PRIMARY_EID);
		dtnClient = getDtnClient(PRIMARY_EID, this);
		SwifiicLogger.logMessage(PRIMARY_EID, dtnClient.getConfiguration(), logFileName);

		// logger.log(Level.SEVERE, "LOGT_TEST");
		// logger.log(Level.INFO, dtnClient.getConfiguration());
		// logNew.info(dtnClient.getConfiguration());
	}

	static boolean exitFlag = false;

	public static void main(String args[]) throws IOException {
		final Suta suta = new Suta();

		// schedule the task to run starting now and then every hour...
		final Runnable runnable = new Runnable() {
			int seqno=1;
			public void run() {
				ExtendedClient ec = suta.getDtnClientInstance().getEC();
				if(!ec.isConnected()){
					System.err.println("SUTA attempting reconnect with the service");
					SwifiicLogger.logMessage(PRIMARY_EID, "SUTA attempting reconnect with the service", errorFileName);
					suta.getDtnClientInstance().reconnect();
				}
				String userList = Helper.getAllUsers();
				//here account detais also contains heartbeat(updated)
				String accountDetails = Helper.getAccountDetailsForAll();

				Notification notif = new Notification("DeviceListUpdate","SUTA", "TODO", "0.1", "Hub");
				notif.addArgument("userList", userList);
				notif.addArgument("accountDetails",accountDetails);
				//notif.addArgument("heartBeat",heartBeat);
				Calendar c = Calendar.getInstance();
		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		        String strDate = sdf.format(c.getTime());
		        notif.addArgument("currentTime",strDate);
		        notif.addArgument("sequenceNumber",seqno+"");

		        // System.out.println("Notification Sent with Sequence Number " + seqno + " at " + strDate);
		        // logNew.info("Notification Sent with Sequence Number " + seqno + " at " + strDate);
				SwifiicLogger.logMessage(PRIMARY_EID, "Notification Sent with Sequence Number " + seqno + " at " + strDate, logFileName);

				String payload = Helper.serializeNotification(notif);
				suta.sendGrp("dtn://in.swifiic.plat.app.suta.andi/mc", payload);
				logNew.info("Sending payload to  dtn://in.swifiic.plat.app.suta.andi/mc"
						+ payload);
				   seqno++;

			}
		};
		/***
		 * Period is the period of update from suta hub to suta andi.
		 * Scheduled Executor serivce is used to run the task periodically
		 */
		int period = Integer.parseInt(sutaProperties.getProperty("suta.periodicInterval"));
		ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(runnable, 0, period, TimeUnit.MINUTES);

	}


	@Override
	/***
	 * @author aarthi
	 * @param payload
	 * @param ctx
	 * @param url -----> Source url of the bundle. Used to extract the dtn id of the user
	 * Source url of the bundle got in Abstract API handler
	 */
	public void handlePayload(String payload, final Context ctx, String url) {
		super.handlePayload(payload, ctx, url);
		int i;
		i = url.lastIndexOf("/");
		final String dtnId = url.substring(0, i);
		final String message = payload;

		SwifiicLogger.logMessage(PRIMARY_EID, "Payload received:\n" + payload, logFileName);

		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {

					Action action = Helper.parseAction(message);
					if (null == action) {
						return;
					}
					String opName = action.getOperationName();
					SwifiicLogger.logMessage(PRIMARY_EID, "got op of" + opName, logFileName);
					if(opName.compareTo("DeviceListUpdate")==0) {
						return;
					}
					String fromUser = action.getArgument("fromUser");
					if (opName.compareTo("RequestApp")==0) { //2ASK: why does this work? //change to requestapp
						SwifiicLogger.logMessage(PRIMARY_EID, "Processing request for App", logFileName);

						String appRequested = action.getArgument("appRequested");
						String appPath = null;
						if (appRequested.compareTo("Msngr")==0) {
							appPath = logDirPath + "msngr.apk";
						} else if (appRequested.compareTo("Bromide")==0) {
							appPath = logDirPath + "bromide.apk";
						} else {
							SwifiicLogger.logMessage(PRIMARY_EID, "AppNotFound", errorFileName);
							return;
						}
						try {
							String encodedApk = Base64.encodeFromFile(appPath);
							SwifiicLogger.logMessage(PRIMARY_EID, "Encoding successful", logFileName);
							//get dtn id for src
							String deviceDTNId = Helper.getDeviceDtnIdForUser(fromUser, ctx);
							Notification notif = new Notification("SendAPKMessage", "SUTA", "NOPE", "0.1", "Hub"); //change to sendAPK
							notif.addArgument("encodedApk", encodedApk); //change to encodedApk
							notif.addArgument("appFileName", appRequested);
							String payload = Helper.serializeNotification(notif);
							send(deviceDTNId+"/in.swifiic.plat.app.suta.andi", payload);
						} catch (IOException e) {
							SwifiicLogger.logMessage(PRIMARY_EID, "Unsuccessfully processed!" + e.getStackTrace(), errorFileName);
						}
						return;
					}

					// We are looking for Op Name "SendInfo" "SendMessage"
					String actualContent = action.getArgument("message");
					String fileName = action.getArgument("filename");
					String macId = action.getArgument("macAddress");
					String notifSentBySutaAt = action.getArgument("notifSentBySutaAt");
					String timeAtHubOfLastHubUpdate = action.getArgument("timeAtHubOfLastHubUpdate");
					String timeAtSutaOfLastHubUpdate = action.getArgument("timeAtSutaOfLastHubUpdate");


					if (macId != null && notifSentBySutaAt != null && dtnId != null
							&& fromUser != null) {

						logNew.info("SUTA ANDI INFO RECEivED.....");
					logNew.info(macId + ":" + notifSentBySutaAt + ":" + dtnId
								+ ":" + fromUser);
						Helper.updateDatabase(macId, notifSentBySutaAt, dtnId, fromUser,timeAtHubOfLastHubUpdate,timeAtSutaOfLastHubUpdate);

					}
					if (fileName != null && actualContent != null) {
						// logNew.info("Log file received with message "
						// 		+ fileName + "\n");
						SwifiicLogger.logMessage(PRIMARY_EID, "Log file received with message "
											+ fileName + "\n", logFileName);
						String folderpath = sutaProperties.getProperty("suta.trackAppFilePath");
						String filepath = folderpath + fileName;
						b64StringToFile(actualContent, filepath);
					}
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
	}

	static void b64StringToFile(String contentB64, String fileName) {
		File writeFile = new File(fileName);
		FileOutputStream str = null;
		try {
			str = new FileOutputStream(writeFile);
			str.write(Base64.decode(contentB64));

		} catch (Exception e) {
			SwifiicLogger.logMessage(PRIMARY_EID, "b64StringToFile: Could not save file "
										+ e.getLocalizedMessage(), errorFileName);
			// logger.log(Level.INFO, "b64StringToFile", "Could not save file "
			// 		+ e.getLocalizedMessage());
			// logNew.error("b64StringToFile:Could not save file "
			// 		+ e.getLocalizedMessage());
		} finally {
			try {
				if (null != str)
					str.close();
			} catch (Exception e) {/* do nothing */
			}
		}
	}
}
