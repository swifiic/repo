package in.swifiic.plat.helper.hub;

import java.io.IOException;
import java.io.*;

import java.util.logging.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.SimpleFormatter;

import ibrdtn.api.object.Bundle;
import ibrdtn.api.object.EID;
import ibrdtn.api.object.GroupEndpoint;
import ibrdtn.api.object.PayloadBlock;
import ibrdtn.api.object.SingletonEndpoint;
import ibrdtn.example.api.Constants;
import ibrdtn.example.api.DTNClient;

public class Base {

	private static final Logger LOGGER = Logger.getLogger(Base.class.getName());
	private static Formatter simpleFormatter = null;
	private static FileHandler fileHandler = null;
	private final String logDirPath = "/home/nic/logfolder/";
	private final String statusLogFilePath = logDirPath + "mylogfile";
	private final String msgLogFilePath = logDirPath + "msg_log";
	private String derivedClass = null;

	private DTNClient dtnClient;

	public void logMessage(String className, String message, String filePath) { //make syncronized
		try {
			fileHandler = new FileHandler(filePath, true);
			fileHandler.setFormatter(new SimpleFormatter());
			fileHandler.setLevel(Level.ALL);
			LOGGER.addHandler(fileHandler);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "FileHandler Exception", e);
		}
		LOGGER.setLevel(Level.ALL); //take this from a file (or load at runtime);
		LOGGER.info(className + ": " + message);

		try {
			fileHandler.close();
		} catch (SecurityException e) {
			LOGGER.log(Level.SEVERE, "Unable to close file handler!");
		}
	}

	public Base() {
		derivedClass = "Anonymous class";
		logMessage(derivedClass, "initiated.", statusLogFilePath);
	}

	public Base(String className) {
		derivedClass = className;
		logMessage(derivedClass, "initiated.", statusLogFilePath);
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

		logMessage(derivedClass, "Sending a bundle to: " + destination.toString() + "\n with data: " + message, msgLogFilePath);
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
		logMessage(derivedClass, "Sending a group bundle to: " + destination.toString() + "\n with data: " + message, msgLogFilePath);
        //System.out.println("Sending a bundle to  Group: " + destination.toString() + "\n with data: " + message);
        dtnClient.send(finalBundle);
    }
}
