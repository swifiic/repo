package in.swifiic.plat.app.suta.hub;

import ibrdtn.api.ExtendedClient;
import ibrdtn.example.api.DTNClient;
import in.swifiic.plat.helper.hub.Base;
import in.swifiic.plat.helper.hub.Helper;
import in.swifiic.plat.helper.hub.xml.Action;
import in.swifiic.plat.helper.hub.xml.Notification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.log4j.PropertyConfigurator;

import de.tubs.ibr.dtn.util.Base64;


public class Suta extends Base {
	
	private static final Logger logger = LogManager.getLogManager().getLogger("");
    public static org.apache.log4j.Logger log=org.apache.log4j.Logger.getLogger("in.swifiic.plat.app.suta.hub.Suta");
 
     
    // Following is the name of the endpoint to register with
    protected String PRIMARY_EID = "suta";
    
    public Suta() {
    	super(/*Executors.newCachedThreadPool()*/);
        this.executor = Executors.newCachedThreadPool();
    	
       	this.client = new ExtendedClient(); 
    	this.dtnClient = new DTNClient(PRIMARY_EID, PAYLOAD_TYPE, HANDLER_TYPE, this, client);
        
        logger.log(Level.INFO, dtnClient.getConfiguration());
        log.info(dtnClient.getConfiguration());
        
    }
    
    static boolean exitFlag=false;
    public static void main(String args[]) throws IOException {
    	LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINE);
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
        		// Mark bundle as delivered...                    
                logger.log(Level.INFO, "Attempted to {0} send to {1}", 
                		new Object[] {payload, "dtn://in.swifiic.plat.app.suta.andi/mc"});
                
               log.info("Sending payload to  dtn://in.swifiic.plat.app.suta.andi/mc"+payload); 
              // log.info("Attempted to {0} send to {1}", new Object[] {payload, "dtn://in.swifiic.plat.app.suta.andi/mc"});
                
	    	}
	    }
    }
    public static void b64StringToFile(String contentB64, String fileName){
		File writeFile = new File(fileName);
		FileOutputStream str =null;
		try {
			str = new FileOutputStream(writeFile);
			str.write(Base64.decode(contentB64));
			
		} catch (Exception e) {
			logger.log(Level.INFO,"b64StringToFile", "Could not save file " + e.getLocalizedMessage());
			log.error("b64StringToFile:Could not save file " + e.getLocalizedMessage());
		} finally {
			try {
				if(null != str) str.close();
			} catch (Exception e) {/* do nothing */	}
		}
		
		
		
	}

	@Override
	public void endPayload() {
		super.endPayload();
		String payload = getPayloadStr();
		final String message = payload;
		
		Action action = Helper.parseAction(message); 	
		if(null==action)
			return;
       	String actualContent=action.getArgument("message");
    	System.out.println("aC:"+actualContent);
    	String fileName=action.getArgument("filename");
    	System.out.println(fileName);
    	log.info("Log file received with message \n"+fileName);
    	if(fileName!=null) {
    		
    	String folderpath="/home/aarthi/Desktop/tmp/";
    	String filepath=folderpath + fileName;
		b64StringToFile(actualContent, filepath); }
       
	}}