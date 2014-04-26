package in.swifiic.hub.app.suta;

import ibrdtn.example.api.DTNClient;

import in.swifiic.hub.lib.Base;
import in.swifiic.hub.lib.Helper;
import in.swifiic.hub.lib.SwifiicHandler;
import in.swifiic.hub.lib.xml.Notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Suta extends Base implements SwifiicHandler {
	
	private static final Logger logger = LogManager.getLogManager().getLogger("");
    private DTNClient dtnClient;
    
    protected ExecutorService executor = Executors.newCachedThreadPool();
    
    // Following is the name of the endpoint to register with
    protected String PRIMARY_EID = "suta";
    
    public Suta() {
        // Initialize connection to daemon
        dtnClient = getDtnClient(PRIMARY_EID, this);
        logger.log(Level.INFO, dtnClient.getConfiguration());
    }
    
    static boolean exitFlag=false;
    public static void main(String args[]) throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	Suta suta = new Suta();
    	Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { Suta.exitFlag=true; }
		 });
    	
    	if(args.length>0 && args[0].equalsIgnoreCase("-D")) { // daemon mode
    		while(!Suta.exitFlag) {
    			try {
					Thread.sleep(60);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
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
	    		// TODO Send broadcast to devices
	    		List<String> deviceList = Helper.getDevicesForAllUsers();
	    		String userList = Helper.getAllUsers();
	    		Notification notif = new Notification("DeviceListUpdate", "SUTA", "TODO", "0.1", "Hub");
	    		notif.addArgument("userList", userList);
	    		String payload = Helper.serializeNotification(notif);
	    		for(int i = deviceList.size()-1; i >= 0; --i) {
            		suta.send(deviceList.get(i) + "/in.swifiic.android.app.suta", payload);
            		// Mark bundle as delivered...                    
                    logger.log(Level.INFO, "Attempted to {0} send to {1}", 
                    				new Object[] {payload, deviceList.get(i) + "/in.swifiic.android.app.suta"});
            	}
	    	}
	    }
    }
    

	@Override
	public void handlePayload(String payload, final Context ctx) {
		System.out.println("Got Message:" + payload);	
	}
}