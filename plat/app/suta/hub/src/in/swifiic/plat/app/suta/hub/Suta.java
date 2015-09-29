package in.swifiic.plat.app.suta.hub;

import ibrdtn.example.api.DTNClient;
import in.swifiic.plat.helper.hub.Base;
import in.swifiic.plat.helper.hub.Helper;
import in.swifiic.plat.helper.hub.SwifiicHandler;
import in.swifiic.plat.helper.hub.xml.Notification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import in.swifiic.plat.helper.hub.xml.Action;


import org.apache.commons.codec.binary.Base64;





public class Suta extends Base implements SwifiicHandler {

	private static final Logger logger = LogManager.getLogManager().getLogger("");
	public static org.apache.logging.log4j.Logger logNew=org.apache.logging.log4j.LogManager.getLogger("in.swifiic.plat.app.suta.hub.Suta");
	private static String dtnId=null;
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

	static boolean exitFlag=false;
	public static void main(String args[]) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Suta suta = new Suta();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { Suta.exitFlag = true; }
		});

		if(args.length>0 && args[0].equalsIgnoreCase("-D")) { // daemon mode
			while(!Suta.exitFlag) {
				try {
					Thread.sleep(60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} else 	while(!Suta.exitFlag) {
			String input;
			System.out.print("Enter \"exit\" to exit application and \"send\" to send broadcast to devices: ");
			input = br.readLine();
			if(input.equalsIgnoreCase("exit")) {
				suta.exit();
			} else if(input.equalsIgnoreCase("send")) {
				String userList = Helper.getAllUsers();
				Notification notif = new Notification("DeviceListUpdate", "SUTA", "TODO", "0.1", "Hub");
				notif.addArgument("userList", userList);
				String payload = Helper.serializeNotification(notif);
				suta.sendGrp("dtn://in.swifiic.plat.app.suta.andi/mc", payload);

				logNew.info("Sending payload to  dtn://in.swifiic.plat.app.suta.andi/mc"+payload);
				// logger.log(Level.INFO, "Attempted to {0} send to {1}", 
				//new Object[] {payload, "dtn://in.swifiic.plat.app.suta.andi/mc"});
			}
		}
	}

	@Override
	/***
	 * @author aarthi
	 * @param payload
	 * @param ctx
	 * @param url -----> Source url of the bundle. Used to extract the dtn id of the user
	 * Source url of the bundle got in Abstract API handler
	 */
	public void handlePayload(String payload, final Context ctx,String url) {
		int i;
		i=url.lastIndexOf("/");
		dtnId=url.substring(0,i);
		System.out.println(dtnId);

		final String message=payload;
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {


					Action action = Helper.parseAction(message); 	
					if(null==action)
						return;
					String actualContent=action.getArgument("message");
					String fileName=action.getArgument("filename");
					String macId =action.getArgument("macAddress");
					String time=action.getArgument("dateTime");
					String fromUser=action.getArgument("fromUser");
					String dtnId=Suta.this.dtnId;
					if(macId!=null && time!=null && dtnId!=null && fromUser!=null)
					{
						
					System.out.println("SUTA ANDI INFO RECEivED.....");
					System.out.println(macId+":"+time+":"+dtnId+":"+fromUser);
					Helper.updateDatabase(macId,time,dtnId,fromUser);
									
					}
					if(fileName!=null && actualContent!=null) 
					{
						logNew.info("Log file received with message "+fileName+"\n");
						String folderpath="/home/aarthi/Desktop/tmp/";
						String filepath=folderpath + fileName;
						b64StringToFile(actualContent, filepath); 
					}
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
			}
		});
	}


	static void b64StringToFile(String contentB64, String fileName){
		File writeFile = new File(fileName);
		FileOutputStream str =null;
		try {
			str = new FileOutputStream(writeFile);
			str.write((new Base64()).decode(contentB64));

		} catch (Exception e) {
			logger.log(Level.INFO,"b64StringToFile", "Could not save file " + e.getLocalizedMessage());
			logNew.error("b64StringToFile:Could not save file " + e.getLocalizedMessage());
		} finally {
			try {
				if(null != str) str.close();
			} catch (Exception e) {/* do nothing */	}
		}
	}
}
