package in.swifiic.hub.lib;

import ibrdtn.api.object.Bundle;
import ibrdtn.api.object.EID;
import ibrdtn.api.object.PayloadBlock;
import ibrdtn.api.object.SingletonEndpoint;
import ibrdtn.example.api.Constants;
import ibrdtn.example.api.DTNClient;

public class Base {
	
	private DTNClient dtnClient;
	
	public DTNClient getDtnClient(String PRIMARY_EID, SwifiicHandler hndlr) {
		dtnClient = new DTNClient(PRIMARY_EID, hndlr);
        return dtnClient;
    }

    public void setDtnClient(DTNClient client) {
        this.dtnClient = client;
    }
    
    private void exit() {
        dtnClient.shutdown();
        System.exit(0);
    }
    
    protected void send(String destinationAddress, String message) {
        EID destination = new SingletonEndpoint(destinationAddress);

        // Create bundle to send
        Bundle bundle = new Bundle(destination, Constants.LIFETIME);
        bundle.setPriority(Bundle.Priority.NORMAL);
        bundle.appendBlock(new PayloadBlock(message.getBytes()));

        final Bundle finalBundle = bundle;
        
        dtnClient.send(finalBundle);    	
    }
}
