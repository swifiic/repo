package in.swifiic.plat.app.suta.hub;

import ibrdtn.example.api.DTNClient;
import in.swifiic.plat.helper.hub.Base;
import in.swifiic.plat.helper.hub.Helper;
import in.swifiic.plat.helper.hub.SwifiicHandler;
import in.swifiic.plat.helper.hub.xml.Notification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class Suta extends Base implements SwifiicHandler {
	public static Properties sutaProperties=null;
	static
	{
                String filePath = " Not Set ";
                try {
                        Properties dbProperties=new Properties();
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

	private static final Logger logger = LogManager.getLogManager().getLogger(
			"");
	public static org.apache.logging.log4j.Logger logNew = org.apache.logging.log4j.LogManager
			.getLogger("in.swifiic.plat.app.suta.hub.Suta");
	private static String dtnId = null;
	private DTNClient dtnClient;

	protected ExecutorService executor = Executors.newCachedThreadPool();
	// Following is the name of the endpoint to register with
	protected String PRIMARY_EID = "suta";

	public Suta() {
		// Initialize connection to daemon
		dtnClient = getDtnClient(PRIMARY_EID, this);
		logger.log(Level.INFO, dtnClient.getConfiguration());
		logNew.info(dtnClient.getConfiguration());
	}

	static boolean exitFlag = false;

	public static void main(String args[]) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		final Suta suta = new Suta();
		
		// schedule the task to run starting now and then every hour...
		final Runnable runnable = new Runnable() {
			int seqno=1;
			public void run() {
				String userList = Helper.getAllUsers();
				Notification notif = new Notification("DeviceListUpdate",
						"SUTA", "TODO", "0.1", "Hub");
				notif.addArgument("userList", userList);
				Calendar c = Calendar.getInstance();
		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		        String strDate = sdf.format(c.getTime());
		        notif.addArgument("currentTime",strDate);
		        notif.addArgument("sequenceNumber",seqno+"");
		     
		        System.out.println("Notification Sent with Sequence Number"+seqno+"at"+strDate);
		        logNew.info("Notification Sent with Sequence Number"+seqno+"at"+strDate);
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
		int i;
		i = url.lastIndexOf("/");
		dtnId = url.substring(0, i);
		final String message = payload;
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {

					Action action = Helper.parseAction(message);
					if (null == action)
						return;
					String actualContent = action.getArgument("message");
					String fileName = action.getArgument("filename");
					String macId = action.getArgument("macAddress");
					String time = action.getArgument("dateTime");
					String fromUser = action.getArgument("fromUser");
					String dtnId = Suta.this.dtnId;
					if (macId != null && time != null && dtnId != null
							&& fromUser != null) {

						logNew.info("SUTA ANDI INFO RECEivED.....");
					logNew.info(macId + ":" + time + ":" + dtnId
								+ ":" + fromUser);
						Helper.updateDatabase(macId, time, dtnId, fromUser);

					}
					if (fileName != null && actualContent != null) {
						logNew.info("Log file received with message "
								+ fileName + "\n");
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
			logger.log(Level.INFO, "b64StringToFile", "Could not save file "
					+ e.getLocalizedMessage());
			logNew.error("b64StringToFile:Could not save file "
					+ e.getLocalizedMessage());
		} finally {
			try {
				if (null != str)
					str.close();
			} catch (Exception e) {/* do nothing */
			}
		}
	}
}
