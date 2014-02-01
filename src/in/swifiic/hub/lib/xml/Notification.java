package in.swifiic.hub.lib.xml;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Notification extends Operation{
	static final String TAG = "XML:Operation";
	private static final Logger logger = Logger.getLogger(Notification.class.getName());

	public Notification(Action action ){
		super();
		opName = action.opName;
		appName=action.appName;
		appId = action.appId;
		appVer = action.appVer;
		arguments = action.arguments;
		deviceId = "TODO"; // TODO - get from runtime context
		userId = "TODO"; // TODO - get from runtime context
		version = "0.1"; // in future handle backward compatability - Move to constants
		
	}
	
	public boolean addArgument(String argName, String argVal){
		// TODO check duplicates
		Argument arg = new Argument();
		arg.argName = argName;
		arg.argValue= argVal;
		int length = arguments.size();
		arguments.add(arg);
		
		logger.log(Level.INFO, "added argument {0} after {1} enteries", 
					new Object[]{argName, Integer.toString(length)} );
		return true;
	}
	
	public boolean deleteArgument(String argName){
		for(int i =0; i<arguments.size(); i++) {
			if(arguments.get(i).argName.equals(argName)){
				arguments.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public void updateNotificatioName(String name) {
		opName = name;
	}
	
	public boolean addFile(String fileContent) {
		fileData = fileContent; // TODO - ensure base64 conversion 
		return true;
	}
	
	public Notification() {
		
	}
}
