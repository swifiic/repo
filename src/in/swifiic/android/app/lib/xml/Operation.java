package in.swifiic.android.app.lib.xml;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.util.Log;

public class Operation {
	static final String TAG="XmlParser:Operation";
	
	@Attribute
	String appName;
	
	@Attribute
	String opName;
	
	@Attribute(required=false)
	String version = "0.1";  // SWiFiIC version "0.1"

	@Attribute
	String userId;   // endpoint user id

	@Attribute(required=false)
	String appVer = "0.1";    // TODO - partially implemented now

	@Attribute
	String deviceId;  // optional for now

	@Attribute
	String appId;     // optional for now
	
	
	@ElementList
	public List<Argument> arguments=new ArrayList<Argument>();
	
	@Element(required=false)
	String fileData;

	public Operation() {
		
	}
	
	
	
	public String getAppName()
	{
		return appName;
	}
	
	/**
	 * For constructing the Action PDU
	 * @param Name
	 */
}
