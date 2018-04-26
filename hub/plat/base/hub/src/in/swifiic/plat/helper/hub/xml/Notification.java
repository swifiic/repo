package in.swifiic.plat.helper.hub.xml;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Notification extends Operation {
	
	static final String TAG = "XML:Operation";
	private static final Logger logger = Logger.getLogger(Notification.class.getName());
	
	// Empty default constructor - SimpleXML Fx goes crazy if this is not here
	public Notification() {
		super();
	}
	
	public Notification(String opName, String appName, String appId, String appVer, String origUsr) {
		super();
		this.opName = opName;
		this.appName = appName;
		this.appId = appId;
		this.appVer = appVer;
		this.deviceId = "TODO"; // TODO - get from runtime context
		this.origUsr = origUsr;
		this.version = "0.1"; // in future handle backward compatability - Move to constants
	}

	public Notification(Action action ){
		super();
		opName = action.opName;
		appName = action.appName;
		appId = action.appId;
		appVer = action.appVer;
		arguments = action.arguments;
		deviceId = "TODO"; // TODO - get from runtime context
		origUsr = action.origUsr;
		version = "0.1"; // in future handle backward compatability - Move to constants
		if(null != action.fileData)
			if(action.fileData.length()>0) fileData = action.fileData;
	}
	
	public boolean addArgument(String argName, String argVal) {
		// Checking for duplicates - return false if duplicate is found
		for(int i = arguments.size()-1; i > 0; --i) {
			if(arguments.get(i).argName.equals(argName)){
				logger.log(Level.WARNING, "Duplicate Argument" + arguments.get(i).argName + " - Not adding.");
				return false;
			}
		}
		Argument arg = new Argument();
		arg.argName = argName;
		arg.argValue= argVal;
		int length = arguments.size();
		arguments.add(arg);
		logger.log(Level.INFO, "added argument {0} after {1} enteries",	new Object[]{argName, Integer.toString(length)} );
		return true;
	}
	
	public boolean deleteArgument(String argName){
		for(int i = arguments.size()-1; i >= 0; --i) {
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
}
