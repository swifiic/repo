package swilet.messenger;

import ibrdtn.example.api.DTNClient;

import in.swifiic.hub.lib.Base;
import in.swifiic.hub.lib.Helper;
import in.swifiic.hub.lib.SwifiicHandler;
import in.swifiic.hub.lib.xml.Action;
import in.swifiic.hub.lib.xml.Notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Messenger extends Base implements SwifiicHandler {
	
	private static final Logger logger = LogManager.getLogManager().getLogger("");
    private DTNClient dtnClient;
    
    protected ExecutorService executor = Executors.newCachedThreadPool();
    
    // Following is the name of the endpoint to register with
    protected String PRIMARY_EID = "messenger";
    
    public Messenger() {
        // Initialize connection to daemon
        dtnClient = getDtnClient(PRIMARY_EID, this);
        logger.log(Level.INFO, dtnClient.getConfiguration());
    }
    
    public static void main(String args[]) throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	Messenger messenger = new Messenger();
    	String input;
    	while(true) {
		    System.out.print("Enter \"exit\" to exit application: ");
	    	input = br.readLine();
	    	if(input.equalsIgnoreCase("exit")) {
	    		messenger.exit();
	    	}
	    }
    	//messenger.exit();
    }

	@Override
	public void handlePayload(String payload, final Context ctx) {
		final String message = payload;
		System.out.println("Got Message:" + payload);
		executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                	String destURL = "dtn://atgrnd.dtn/" + "in.swifiic.android.app.msngr";
                	
                	Action action = Helper.parseAction(message);
                	Notification notif = new Notification(action);
                	notif.updateNotificatioName("DeliverMessage");
                	// TODO extract the destination users
                	// TODO map to their devices
                	// TODO send it out to all devices for that user
                	// library should provide something like List<String> getDestinations(String username, String app, String role)
                	
                	// send("dtn://shivam/in.swifiic.android.app.msngr", Helper.serializeNotification(notif));
                	String response = Helper.serializeNotification(notif);
                    send(ctx.srcUrl, response);
                    // Mark bundle as delivered...                    
                    logger.log(Level.SEVERE, "Attempted to send: to {1}, had received \n{0}\n and responsed with \n {2}", 
                    				new Object[] {message, destURL, response});
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unable to process message and send response\n" +message, e);
                }
            }
        });
		
	}
}
