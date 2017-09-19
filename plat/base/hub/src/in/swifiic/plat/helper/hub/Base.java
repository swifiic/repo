package in.swifiic.plat.helper.hub;

import java.io.IOException;
import java.io.*;

import java.util.logging.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
	private static Handler fileHandler = null;

	public Base() {
		// File file = new File("/home/nic/logfolder/xdx");
		//
		// if (file.createNewFile()){
		// System.out.println("File is created!");
		// }
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		try {
			fileHandler = new FileHandler("/home/nic/logfolder/mylogfile");
			fileHandler.setFormatter(simpleFormatter);
			fileHandler.setLevel(Level.ALL);
			LOGGER.addHandler(fileHandler);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "FileHandler Exception2", e);
		}
		simpleFormatter = new SimpleFormatter();

		LOGGER.setLevel(Level.ALL);

		LOGGER.info("Anon class initiated");
	}

	// public Base (String className) {
	// 	try {
	// 		fileHandler = new FileHandler("./mylogfile2");
	// 		fileHandler.setFormatter(simpleFormatter);
	// 		fileHandler.setLevel(Level.ALL);
	// 	} catch (IOException e) {
	// 		LOGGER.log(Level.SEVERE, "FileHandler Exception2", e);
	// 	}
	// 	simpleFormatter = new SimpleFormatter();
	//
	// 	LOGGER.setLevel(Level.ALL);
	//
	// 	LOGGER.addHandler(fileHandler);
	// 	LOGGER.info(className + " class initiated");
	// }

	private DTNClient dtnClient;

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
