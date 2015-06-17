package in.swifiic.plat.helper.andi.xml;

import android.util.Log;
import in.swifiic.plat.helper.andi.AppEndpointContext;
import in.swifiic.plat.helper.andi.xml.Argument;

public class Action extends Operation{
	static final String TAG = "XML:Operation";

	public Action(){
		super();
	}
	
	public Action(String name, AppEndpointContext apCtx){
		super();
		opName = name;
		appName = apCtx.appName;
		appId = apCtx.appId;
		appVer = apCtx.appVer;
		deviceId = "TODO"; // TODO - get from runtime context
		origUsr = "shivam"; // TODO - get from runtime context
		version = "0.1"; // in future handle backward compatability - Move to constants
	}
	
	public boolean addArgument(String argName, String argVal){
		// TODO check duplicates
		Argument arg = new Argument();
		arg.argName = argName;
		arg.argValue= argVal;
		int length = arguments.size();
		if(null == arguments){
			
		}
		arguments.add(arg);
		Log.d(TAG, "added argument " + argName + " after" + length + "entries");
		return true;
	}
	
	public boolean addFile(String fileContent) {
		fileData = fileContent; // TODO - ensure base64 conversion 
		return true;
	}
}
