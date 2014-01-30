package swilet.messenger;

import ibrdtn.api.object.Bundle;
import ibrdtn.api.object.EID;
import ibrdtn.api.object.PayloadBlock;
import ibrdtn.api.object.SingletonEndpoint;

import ibrdtn.example.api.Constants;
import ibrdtn.example.api.DTNClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Messenger {
	private static final Logger logger = LogManager.getLogManager().getLogger("");
    private DTNClient dtnClient;
    protected String PRIMARY_EID = "messenger";
    
    public Messenger() {
        // Initialize connection to daemon
        dtnClient = new DTNClient(PRIMARY_EID);
        logger.log(Level.INFO, dtnClient.getConfiguration());
    }
    
    public DTNClient getDtnClient() {
        return dtnClient;
    }

    public void setDtnClient(DTNClient client) {
        this.dtnClient = client;
    }
    
    private void exit() {
        dtnClient.shutdown();
        System.exit(0);
    }
    
    private void send(String destinationAddress, String message) {
        EID destination = new SingletonEndpoint(destinationAddress);

        // Create bundle to send
        Bundle bundle = new Bundle(destination, Constants.LIFETIME);
        bundle.setPriority(Bundle.Priority.NORMAL);
        bundle.appendBlock(new PayloadBlock(message.getBytes()));
        dtnClient.send(bundle);    	
    }
    
    public static void main(String args[]) throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	Messenger messenger = new Messenger();
    	String destination, message;
    	while(true) {
		    System.out.print("Enter destination: ");
	    	destination = br.readLine();
	    	System.out.print("Enter message: ");
	    	message = br.readLine();
	    	messenger.send(destination, message);
	    }
    	//messenger.exit();
    }
}
