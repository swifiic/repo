package in.swifiic.plat.helper.andi;


import java.util.List;

public class AppEndpointContext {
	public AppEndpointContext(String appName, String appVer, String appId){
		this.appId = appId;
		this.appVer = appVer;
		this.appName = appName;
	}
	public String appName;
	public String appVer;
	public String appId;
	public List <String> appActions;
	public List <String> appNotifications;
}
