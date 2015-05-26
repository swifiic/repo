package in.swifiic.plat.helper.hub;

import java.util.concurrent.ExecutorService;

import ibrdtn.api.ExtendedClient;
import ibrdtn.api.object.Bundle;
import ibrdtn.api.object.EID;
import ibrdtn.api.object.GroupEndpoint;
import ibrdtn.api.object.PayloadBlock;
import ibrdtn.api.object.SingletonEndpoint;
import ibrdtn.example.api.APIHandlerType;
import ibrdtn.example.api.Constants;
import ibrdtn.example.api.DTNClient;
import ibrdtn.example.api.PayloadType;
import ibrdtn.example.api.SelectiveHandler;


public class Base extends SelectiveHandler {
	private String payload = null;
    protected PayloadType PAYLOAD_TYPE = PayloadType.BYTE;
    protected APIHandlerType HANDLER_TYPE = APIHandlerType.SELECTIVE;

    //private ExtendedClient tempExC = new ExtendedClient();
	protected DTNClient dtnClient = null;
	
	public Base (/*ExecutorService executor*/) {
		super(/*tempExC, executor,*/ PayloadType.BYTE);
	}
	
	@Override
    public synchronized void endPayload() {
		try {
			payload = new String(bytes);
		} catch (Exception ex){
			System.err.print("Bytes to string failed:" + bytes + "\n");
			payload = null;
		}
		
		super.endPayload();
		// String bundleId = envelope.getBundleID();
		
	}
	
	public String getPayloadStr() {
		String retVal = payload;
		payload=null;
		return retVal;
		
	}
	
	/*public DTNClient getDtnClient(String PRIMARY_EID) {
		dtnClient = new DTNClient(PRIMARY_EID, PAYLOAD_TYPE, HANDLER_TYPE, this);
		this.client = dtnClient.getEC();
        return dtnClient;
    }*/

     
    protected void exit() {
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
        
        System.out.println("Sending a bundle to: " + destination.toString() + "\n with data: " + message);
        dtnClient.send(finalBundle);    	
    }

    protected void sendGrp(String destinationAddress, String message) {
    	EID destination = new GroupEndpoint(destinationAddress);

        // Create bundle to send
        Bundle bundle = new Bundle(destination, Constants.LIFETIME);
        bundle.setPriority(Bundle.Priority.NORMAL);
        bundle.appendBlock(new PayloadBlock(message.getBytes()));

        final Bundle finalBundle = bundle;
        
        //System.out.println("Sending a bundle to  Group: " + destination.toString() + "\n with data: " + message);
        dtnClient.send(finalBundle);    	
    }
}
