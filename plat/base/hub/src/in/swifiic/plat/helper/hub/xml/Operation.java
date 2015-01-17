package in.swifiic.plat.helper.hub.xml;

import java.util.ArrayList;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;


public class Operation {
	static final String TAG="XmlParser:Operation";
	
	@Attribute
	String appName;
	
	@Attribute
	String opName;
	
	@Attribute(required=false)
	String version = "0.1";  // SWiFiIC version "0.1"

	@Attribute
	String origUsr;   // endpoint user id

	@Attribute(required=false)
	String appVer = "0.1";    // TODO - partially implemented now

	@Attribute
	String deviceId;  // optional for now

	@Attribute
	String appId;     // optional for now
	
	
	@ElementList
	public List<Argument> arguments= new ArrayList<Argument>();
	
	@Element(required=false)
	String fileData;

	public String getAppName() {
		return appName;
	}

	public String getOperationName() {
		return opName;
	}

	public Operation() {
		super();
	}
}
