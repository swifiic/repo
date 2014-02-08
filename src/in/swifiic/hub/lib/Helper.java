package in.swifiic.hub.lib;

import in.swifiic.hub.lib.SwifiicHandler.Context;
import in.swifiic.hub.lib.xml.Action;
import in.swifiic.hub.lib.xml.Notification;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class Helper {

	public static Action parseAction(String str) {
        Serializer serializer = new Persister();
        try {
        	Action action = serializer.read(Action.class,str);
        	return action;
        } catch(Exception e) {
        	// This should not happen unless APP tries a random string 
        	// String given from Generic Service was already tested for success
        }
        return null;
	}
	
	public static String serializeNotification(Notification notif) {
        try {
        	 StringWriter writer = new StringWriter();
        	 Serializer serializer = new Persister();  
       	     serializer.write(notif, writer);  

        	 return writer.getBuffer().toString();
        } catch(Exception e) {
        	// This should not happen since Action is a XML serializable object
        }
        return null;
	}

	public static List<String> getDevicesForUser(String user, Context ctx) {
		// TODO - Get this mapping from a Database for the specific app using the context
		// Hardcoding for now
		List<String> deviceList = new ArrayList<String>();
		switch(user) {
		case "shivam":
			deviceList.add("dtn://shivam-nexus");
			return deviceList;
		case "abhishek":
			deviceList.add("dtn://abhishek-grand");
			return deviceList;
		default:
			return null;
		}
	}
}
