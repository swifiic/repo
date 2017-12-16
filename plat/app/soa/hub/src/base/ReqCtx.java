package base;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;

public class ReqCtx {
	/** bunch of this code can possibly be auto generated
	 * TBD - explore XSD Java code generation options - will need to Refactor
	 *  Tried JAXB - seems to be an overkill right now
	 */
	public enum FieldNames {
	    PDU_VERSION("version",	0),
	    USER_ID("userId",			0),
	    DEVICE_ID("deviceId",		0),
	    APP_ID("appId",				0),
	    APP_VER("appVer",			0),
	    PDU_LOG_VISIBILITY("pduLogVisibility",	0), 
	    BILLING("billing",			0),

	    // following are in nested Operation of PDU
	    NAME ("name",				1),
	    MODE ("mode",				1),
	    DELAY ("delay",				1,false),
	    DISCARD_POLICY("discardPolicy",			1),
	    ACCESS("access",			1),
	    OPERATION_ACCESS("operationAccess",		1),
	    LOG_VISIBILITY("LogVisibility",			1), 
	    COUPLED_OPERATIONS("coupledOperations",	1),
	    
	    // following are in arguments nested within Operation
	    ARG_NAME("argName",						2),
	    ARG_VALUE("argValue",					2),
	    
	    // following are development fields for authentication etc.
	    SESSION("session",0);
	    
	    

	    private final String varName;
	    private final boolean pduOptional;
	    private final String defaultVal;
	    private final int level;
	    private final String httpName;

	    private FieldNames(String s) {
	    	httpName = s;
	        varName = s;
	        pduOptional = false;
	        level = 0;
	        defaultVal="no Init";
	    }

	    private FieldNames(String s, int depth) {
	    	httpName = s;
	    	varName = s;
	        pduOptional = false;
	        level = depth;
	        defaultVal="no Init";
	    }

	    private FieldNames(String s, int depth, boolean optional) {
	    	httpName = s;
	        varName = s;
	        pduOptional = optional;
	        level = depth;
	        defaultVal="no Init";
	    }

	    public boolean equalsName(String otherName){
	        return (otherName == null)? false:varName.equals(otherName);
	    }

	    public String toString(){
	       return varName;
	    }
	    
	    public boolean pduOptional() {
	    	return pduOptional;
	    }
	    
	    public int depth() {
	    	return level;
	    }
	    
	    public String defaultVal() {
	    	return defaultVal;
	    }

	    public String httpFieldName() {
	    	return httpName;
	    }

	}
	public static final FieldNames httpMandatoryFields[] = {FieldNames.PDU_VERSION, FieldNames.USER_ID, 
		FieldNames.DEVICE_ID, FieldNames.APP_ID, FieldNames.APP_VER, FieldNames.NAME};
    public static final FieldNames httpOptionalFields[] = {FieldNames.PDU_LOG_VISIBILITY, FieldNames.BILLING};
    public static final FieldNames httpInternalFields[] = {FieldNames.SESSION};

    
    /***Enumeration - to be kept in Sync with XSD
     * *  <xs:simpleType name="RoleType">
     *    "Admin" "Operator" "User"/>
     */
    public enum RoleType {
    	ADMIN("admin"),
    	OPERATOR("operator"),
    	USER("user");
    	private final String roleName;
    	private RoleType(String role){
    		roleName=role;
    	}
    	public String toString(){
  	       return roleName;
  	    }
    }
    
    
    /***Enumeration - to be kept in Sync with XSD
     * *  <xs:simpleType name="Billing">
     *  "Monthly" "None" "PerEvent""Low" "Medium" "High"/>
     */
    public enum Billing {
    	MONTHLY("Monthly"),
    	NONE("None"),
    	PEREVENT("PerEvent"),
    	LOW("Low"),
    	MEDIUM("Medium"),
    	HIGH("High");
    	private final String billName;
    	private Billing(String bill){
    		billName=bill;
    	}
    	public String toString(){
  	       return billName;
  	    }
    }
    
    /***Enumeration - to be kept in Sync with XSD
     * *  <xs:simpleType name="OperationMode">
     *    "SyncOperator" "SyncApp" "AsyncBestEffort" "AsyncNotifyAndAck" "AyncNotifyNoAck"
     */
    public enum OperationMode {
    	SYNCOPERATOR("SyncOperator"),
    	SYNCAPP("SyncApp"),
    	ASYNCBESTEFFORT("AsyncBestEffort"),
    	ASYNCNOTIFYANDACK("AsyncNotifyAndAck"),
    	ASYNCNOTIFYNOACK("AsyncNotifyNoAck");
    	private final String modeName;
    	private OperationMode(String mode){
    		modeName=mode;
    	}
    	public String toString(){
  	       return modeName;
  	    }
    }
  

    /***Enumeration - to be kept in Sync with XSD
     * *  <xs:simpleType name="DelayType">
     *  "Critical" "HiPri" "HiPri" "LowPri"/> 
     */
    public enum Delay {
    	CRITICAL("Critical"),
    	HIPRI("HiPri"),
    	MEDPRI("HiPri"),
    	LOWPRI("LowPri");
    	
    	private final String delayName;
    	private Delay(String delay){
    		delayName=delay;
    	}
    	public String toString(){
  	       return delayName;
  	    }
    }

    
    /***<xs:simpleType name="DiscardType">
     *  < "OnlyOnDeliveryOrTimeout" "OnLowSpaceWarnThreshold" "OnLowCriticalThreshold"/> 
     */
    public enum Discard {
    	ONLYONDELIVERYORTIMEOUT("OnlyOnDeliveryOrTimeout"),
    	ONLOWSPACEWARNTHRESHOLD("OnLowSpaceWarnThreshold"),
    	ONLOWCRITICALTHRESHOLD("OnLowCriticalThreshold");
    	
    	private final String discardName;
    	private Discard(String delay){
    		discardName=delay;
    	}
    	public String toString(){
  	       return discardName;
  	    }
    }

     
    /** Enumeration - to be kept in Sync with XSD
     *  <xs:simpleType name="Visibility">
     *  "OperatorOnly" "Admin" "AdminAndUser" "AdminAndCreator" "User"/ "Public"/>
     *
     * @author abhishek
     *
     */
    public enum LogVisibility {
    	ADMIN("admin"),
    	ADMINANDUSER("AdminAndUser"),
    	ADMINANDCREATOR("AdminAndCreator"),
    	USER("User"),
    	PUBLIC("public");
    	
    	private final String visibilityName;
    	private LogVisibility(String s) {
    		visibilityName = s;
    	}
    	public String toString(){
 	       return visibilityName;
 	    }
    }

 

    private String opName, pduVer, userId, devId, appId, appVer;
    boolean isActivity = false;
 	
 	/**
	 * @return the pduVer
	 */
	public String getPduVer() {
		return pduVer;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @return the devId
	 */
	public String getDevId() {
		return devId;
	}

	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId;
	}

	/**
	 * @return the appVer
	 */
	public String getAppVer() {
		return appVer;
	}

	
	// code borrowed from ActionProcessor.validateAndLogRequest - should have been called before this
	Map<java.lang.String,java.lang.String[]> paramMap;
 	public ReqCtx(HttpServletRequest req, String sesCtx){
 		Map<java.lang.String,java.lang.String[]> paramMap1 = req.getParameterMap();
    	paramMap = new HashMap<java.lang.String,java.lang.String[]>();
    	paramMap.putAll(paramMap1);
 		
    	isActivity = true;
    	pduVer = paramMap.remove(FieldNames.PDU_VERSION.httpFieldName())[0];
    	userId = paramMap.remove(FieldNames.USER_ID.httpFieldName())[0];
    	devId  = paramMap.remove(FieldNames.DEVICE_ID.httpFieldName())[0];
    	appId  = paramMap.remove(FieldNames.APP_ID.httpFieldName())[0];
    	appVer = paramMap.remove(FieldNames.APP_VER.httpFieldName())[0];
    	opName = paramMap.remove(FieldNames.NAME.httpFieldName())[0];
    	
    	// optional FieldNames.PDU_LOG_VISIBILITY, FieldNames.BILLING - ignored for now
    	paramMap.remove(FieldNames.PDU_LOG_VISIBILITY.httpFieldName());
    	paramMap.remove(FieldNames.BILLING.httpFieldName());

    	// remaining parameters should be arguments for operation
    	// leave them in paramMap for now
    	// TBD - parse arguments for App.xml's operation
    	
    	// TBD - to handle file uploads XXX

 	}
 	
    public boolean hasFile() {
    	return false;
    }
    
    public boolean isActivity() {
    	return isActivity;
    }
    
    public String getOpName(){
    	return opName;
    }
    
    public boolean hasArgName(String argName){
    	return paramMap.containsKey(argName);
    }
    
    public String getArgVal(String argName){
    	if(hasArgName(argName))
    		return paramMap.get(argName)[0];
    	else 
    		return null;
    }
    
}
