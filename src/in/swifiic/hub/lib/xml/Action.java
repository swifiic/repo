package in.swifiic.hub.lib.xml;


public class Action extends Operation{
	public Action() {
		super();
	}
	
	public String getNotificationName() {
		return opName;
	}
	
	public boolean hasArgument(String argName) {
		for(int i=0; i < arguments.size(); i++){
			if(arguments.get(i).argName.equals(argName))
				return true;
		}
		return false;
	}
	public String getArgument(String argName) {
		for(int i=0; i < arguments.size(); i++){
			if(arguments.get(i).argName.equals(argName))
				return arguments.get(i).argValue;
		}
		return null;
		
	}

}
