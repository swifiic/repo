package in.swifiic.plat.helper.hub.xml;

public class Action extends Operation{
	public Action() {
		super();
	}
	
	
	public boolean hasArgument(String argName) {
		for(int i = arguments.size()-1; i >= 0; --i){
			if(arguments.get(i).argName.equals(argName))
				return true;
		}
		return false;
	}
	
	public String getArgument(String argName) {
		for(int i = arguments.size()-1; i >= 0; --i){
			if(arguments.get(i).argName.equals(argName))
				return arguments.get(i).argValue;
		}
		return null;
	}
}