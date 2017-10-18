package in.swifiic.plat.helper.hub;

import java.io.*;

import in.swifiic.plat.helper.hub.SwifiicLogger;
// import in.swifiic.plat.helper.hub.Base;
// import in.swifiic.plat.helper.hub.DatabaseHelper;
import in.swifiic.plat.helper.hub.Helper;
import in.swifiic.plat.helper.hub.SwifiicHandler;
import in.swifiic.plat.helper.hub.xml.Action;
// import in.swifiic.plat.helper.hub.xml.Notification;
// import in.swifiic.plat.helper.hub.SwifiicLogger;

import ibrdtn.api.object.Bundle;
import ibrdtn.api.object.EID;
import ibrdtn.api.object.GroupEndpoint;
import ibrdtn.api.object.PayloadBlock;
import ibrdtn.api.object.SingletonEndpoint;
import ibrdtn.example.api.Constants;
import ibrdtn.example.api.DTNClient;

public class Base implements SwifiicHandler {
	private DTNClient dtnClient;

	private final String statusLogFilePath = "base_log";
	private final String msgLogFilePath = "msg_log";
	private String derivedClass = null;

	public Base() {
		derivedClass = "Anonymous class";
		SwifiicLogger.logMessage(derivedClass, "initiated.", statusLogFilePath);
	}

	public Base(String className) {
		derivedClass = className;
		SwifiicLogger.logMessage(derivedClass, "initiated.", statusLogFilePath);
	}

	public DTNClient getDtnClient(String PRIMARY_EID, SwifiicHandler hndlr) {
		dtnClient = new DTNClient(PRIMARY_EID, hndlr);
        return dtnClient;
    }

    public DTNClient getDtnClientInstance() {
        return this.dtnClient ;
    }

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

		SwifiicLogger.logMessage(derivedClass, "Sending a bundle to: " + destination.toString() + "\n with data: " + message, msgLogFilePath);
        // System.out.println("Sending a bundle to: " + destination.toString() + "\n with data: " + message);

        dtnClient.send(finalBundle);
    }

	public void handlePayload(String payload, final Context ctx, String srcurl) {
		Action action = Helper.parseAction(payload);

		String appName = action.getAppName();
		String opName = action.getOperationName();
		String toUser = action.getArgument("toUser");
		String deviceDtnId = Helper.getDeviceDtnIdForUser(toUser, ctx);

		Helper.logHubMessage(appName, opName, srcurl, deviceDtnId);
	}

    protected void sendGrp(String destinationAddress, String message) { //is this used for multicast messages? answer: yes
    	EID destination = new GroupEndpoint(destinationAddress);

        // Create bundle to send
        Bundle bundle = new Bundle(destination, Constants.LIFETIME);
        bundle.setPriority(Bundle.Priority.NORMAL);
        bundle.appendBlock(new PayloadBlock(message.getBytes()));

        final Bundle finalBundle = bundle;
		SwifiicLogger.logMessage(derivedClass, "Sending a group bundle to: " + destination.toString() + "\n with data: " + message, msgLogFilePath);
        //System.out.println("Sending a bundle to  Group: " + destination.toString() + "\n with data: " + message);
        dtnClient.send(finalBundle);
    }
}
