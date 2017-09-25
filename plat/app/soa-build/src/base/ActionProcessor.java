package base;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;

import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import base.ReqCtx.*;

/**
 * Servlet implementation class ActionProcessor: base from which others extend
 * 
 */
public class ActionProcessor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final PrintStream o = System.out;
	
	static Integer counter =0;
	Integer sessionId=0;

    protected synchronized String getSessionId() {
    	if(0==sessionId)  sessionId=++counter;
    	return (sessionId).toString();
    }

    /**
     * @see HttpServlet#HttpServlet()
     */
	public ActionProcessor() {
        super();
        // TODO Auto-generated constructor stub
    }

	private boolean validateAndLogRequest(HttpServletRequest request)
	{
		boolean parseError=false;
		StringBuffer errStr=new StringBuffer("");
    	String sesCtx = getSessionId(); // we need to push it on request
		StringBuffer logStr=new StringBuffer("Log id: " + sesCtx + ":");
    	
    	Map<java.lang.String,java.lang.String[]> paramMap1 = request.getParameterMap();
    	Map<java.lang.String,java.lang.String[]> paramMap = new HashMap<java.lang.String,java.lang.String[]>();
    	paramMap.putAll(paramMap1);
    	
    	String keys[] = new String[paramMap1.size()];
    	paramMap1.keySet().toArray(keys);
    	for (String key:keys){ 
    		o.print(key+" : ");
    		for (String s:paramMap1.get(key)) o.print(s+" ");
    		o.println();
    	}
		
    	for(int i =0; i < ReqCtx.httpMandatoryFields.length; i++){
    		String key = ReqCtx.httpMandatoryFields[i].httpFieldName();
    		if(paramMap.containsKey(key.toString())){
    			String values[]=paramMap.remove(key);
    			if(values.length == 1){
    				logStr.append(key+ ":" + values[0] + " ");
    			} else {
    				parseError=true;
    				errStr.append("Multiple values for key:" + key + ":values" + values.length + "(");
    				for(int j =0; j < values.length; j++) errStr.append(values[j]);
    				errStr.append( ")");
    			}
    		} else {
    			parseError =true;
    			errStr.append("Missing Mandatory field:"+ ReqCtx.httpMandatoryFields[i]+ " ");
    		}
    	}
    	for(int i =0; i < ReqCtx.httpOptionalFields.length; i++){
    		String key = ReqCtx.httpOptionalFields[i].httpFieldName();
    		if(paramMap.containsKey(key)){
    			String values[]=paramMap.remove(key.toString());
    			if(values.length == 1){
    				logStr.append("Optional " + key+ ":" + values[0] + " ");
    			} else {
    				parseError=true;
    				errStr.append("Multiple values for key:" + key + ":values" + values.length + "(");
    				for(int j =0; j < values.length; j++) errStr.append(values[j]);
    				errStr.append( ")");
    			}
    		} 
    	}
    	
    	// remaining parameters are arguments for Operation
    	// TBD - implement verification logic for AppId/Operation so as to verify with App.xml XXX
    	
    	Set<String> keySet = paramMap.keySet();
    	Iterator<String> itr=keySet.iterator();
    	while( itr.hasNext()){
    		String key = itr.next();
    		String values[]=paramMap.get(key);
    		itr.remove();
			if(values.length == 1){
				logStr.append("Extra " + key+ ":" + values[0] + " ");
			} else {
				parseError=true;
				errStr.append("Multiple values for key:" + key + ":values" + values.length + "(");
				for(int j =0; j < values.length; j++) errStr.append(values[j]);
				errStr.append( ")");
			}
    	}
    	    	
    	// TBD - validate the user - may need additional fields - defer for now
    	
    	getServletContext().log(logStr.toString());
    	if(parseError) {
    		getServletContext().log(errStr.toString());
    	} else {
    		request.setAttribute(FieldNames.SESSION.name(), sesCtx);
    	}
    	
		return !parseError;
	}
	
	private void logResponse(HttpServletResponse response){
		boolean parseError=false;
		String errStr="";
    	String sesCtx = getSessionId();
		String logStr="Response Log id: " + sesCtx + ":";
		// Map<java.lang.String,java.lang.String[]> paramMap = response.getStatus().getParameterMap();
	}
	
	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	System.out.println("In service of ActionProcessor");
    	if(validateAndLogRequest(request)) super.service(request, response);
    	else throw new ServletException("Validation Failed");
    	logResponse(response);
	}
    
    /***
     * Helpers for derived classes
     */
	protected void successResponse(ReqCtx ctx, HttpServletResponse response, String msg) throws  IOException {
    	response.setStatus(HttpServletResponse.SC_OK);
    	response.setHeader("Content-Type", "text/xml;charset=UTF-8");
    	String output = "\n <result>" + msg + "</result>"; // TBD XXX convert to response PDU type
        response.getOutputStream().write(output.getBytes("UTF-8"));
        response.getOutputStream().flush();
        getServletContext().log("Success");
	}
	
	protected void  errorResponse(ReqCtx ctx, HttpServletResponse response, String msg) throws  IOException {
		response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "\nMessage: " + msg + "\nOperation:"+ ctx.getOpName()); 
	}

	protected void  errorResponse(ReqCtx ctx, HttpServletResponse response, String msg,int respCode) throws  IOException {
		response.sendError(respCode, "\nMessage: " + msg + "\nOperation:"+ ctx.getOpName()); 
	}
	
    
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response); // TODO handle file uploads from here
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response); // TODO handle file removals from here - mark them removed
	}

}
