package in.swifiic.plat.helper.andi.xml;


public class Notification extends Operation{
	
	public Notification(){
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
