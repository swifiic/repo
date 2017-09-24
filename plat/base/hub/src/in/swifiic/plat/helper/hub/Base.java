package in.swifiic.plat.helper.hub;

import java.io.*;

import in.swifiic.plat.helper.hub.SwifiicLogger;

import ibrdtn.api.object.Bundle;
import ibrdtn.api.object.EID;
import ibrdtn.api.object.GroupEndpoint;
import ibrdtn.api.object.PayloadBlock;
import ibrdtn.api.object.SingletonEndpoint;
import ibrdtn.example.api.Constants;
import ibrdtn.example.api.DTNClient;

public class Base {
	private DTNClient dtnClient;

	private final String logDirPath = "/home/nic/logfolder/";
	private final String statusLogFilePath = logDirPath + "mylogfile";
	private final String msgLogFilePath = logDirPath + "msg_log";
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
