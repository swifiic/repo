package in.swifiic.plat.app.suta.andi.mgmt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.util.Xml;
import android.os.Bundle;
import android.os.SystemClock;

import org.xmlpull.v1.XmlSerializer;


class TAContext{
	final private static  String MY_TAG = "TAC";
	final private static int NumSamples = 15; 
	final private long RX_TX_TSH = 32; //min 32*8 =256 kbps for streaming; some audio streams will go under this   
	final private int FAST_SAMPLE = 30000; // Thirty seconds
	final private int SLOW_SAMPLE = 16* FAST_SAMPLE; 
	final private int NUMBER_NODES=500;//Entries in XML before upload

	static ActivityManager actMgr = null;
	private static TrackService svcRef = null ;
	static DecimalFormat df = new DecimalFormat("#.##");;

	protected LocationManager locationManager=null;


	private int sampleNum =0;
	double mAvgRX = 0;
	double mAvgTX = 0;
	private long sampleTX[] = new long[NumSamples]; 
	private long sampleRX[] = new long[NumSamples]; 
	private long mStartRX = 0;
	private long mStartTX = 0;
	
	boolean updateNotified = false;
	int delayTimeMilliSec=SLOW_SAMPLE;

	ServiceAppCollector sAC = null;
	SpeedInfoCollector sIC = null;

	static TAContext createContextFromPersistedData(TrackService srvcRef){
		// load preferences : possibly for when to check for update
		svcRef = srvcRef;
		actMgr = (ActivityManager)svcRef.getSystemService(Context.ACTIVITY_SERVICE);
		TAContext ctx = new TAContext();
		return ctx;
	}

	TAContext() {
		sAC = new ServiceAppCollector();
		sIC = new SpeedInfoCollector(sAC);
		
		mStartRX = TrafficStats.getTotalRxBytes();
		mStartTX = TrafficStats.getTotalTxBytes();
		if(LoadPreferences("fileuploads").isEmpty())
			SavePreferences("fileuploads","1");
		if(LoadPreferences("createdFileIdx").isEmpty())
			SavePreferences("createdFileIdx","1");

		if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
			AlertDialog.Builder alert = new AlertDialog.Builder(svcRef);
			alert.setTitle("Uh Oh!");
			alert.setMessage("Your device does not support traffic stat monitoring.");
			alert.show();
		}
		if(LoadPreferences("mask").equalsIgnoreCase("off"))
		{
			final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 20; // in Meters
			final long MINIMUM_TIME_BETWEEN_UPDATES = 30000; // in Milliseconds

			if(null == locationManager)
				locationManager = (LocationManager) svcRef.getSystemService(Context.LOCATION_SERVICE);
			if(null == locationManager)
				Log.e(MY_TAG, "Failed to init getSystemService(Context.LOCATION_SERVICE);");
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 
					MINIMUM_TIME_BETWEEN_UPDATES, 
					MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
					new MyLocationListener());
			getCurrentLocation();
		}

		for(int i=0; i < NumSamples; i++)
			sampleTX[i]=sampleRX[i] =0;
	}
	
	void doSampling(){
		//Log.d(MY_TAG,"batterylevel"+batteryPct);
		int nonZeroSampleCount =0;
		String log1 = null, log2 = null; 
		
		// we discard the fact that some samples are smaller time than other - approximation
		long totBytes =0;

		// Receive path logic
		long numBytes = TrafficStats.getTotalRxBytes();
		long rxBytesKBps= ((numBytes-mStartRX )/(delayTimeMilliSec));
		totBytes+=(numBytes-mStartTX);
		mStartRX = numBytes;
		mAvgRX = (rxBytesKBps + NumSamples *  mAvgRX - sampleRX[sampleNum % NumSamples]) / NumSamples;
		if(mAvgRX<0) mAvgRX=0; // offset the old values effecting newer run
		sampleRX[sampleNum%NumSamples] = rxBytesKBps;


		// Transmit path logic
		numBytes = TrafficStats.getTotalTxBytes();
		long txBytesKBps= ((numBytes-mStartTX )/(delayTimeMilliSec));
		totBytes+=(numBytes-mStartTX);
		mStartTX = numBytes;
		mAvgTX = (txBytesKBps + NumSamples *  mAvgTX - sampleTX[sampleNum % NumSamples]) / NumSamples;
		if(mAvgTX<0) mAvgTX=0;
		sampleTX[sampleNum%NumSamples] = txBytesKBps;

		if(++sampleNum == NumSamples) {
			sampleNum =0;
			log1 = "TX:";
			log2 = "RX:";
			for(int i=0; i < NumSamples;i++){ 
				log1 = log1 + df.format(sampleTX[i])+" ";
				log2 = log2 + df.format(sampleRX[i])+" ";
			}
			Log.d("SAMPLES:", log1);
			Log.d("SAMPLES:", log2);
		}

		for(int i=0; i < NumSamples;i++) 
			if(sampleTX[i] > (RX_TX_TSH/2) || sampleRX[i] > (RX_TX_TSH/2))nonZeroSampleCount++;

		if((txBytesKBps + rxBytesKBps) > (1.2 * RX_TX_TSH)){ /* 1.2 factor for hysteresis: quickly latch on to speed up */
			delayTimeMilliSec =FAST_SAMPLE;
		} else if( (nonZeroSampleCount <= (NumSamples/4)) && 
				((mAvgRX + mAvgTX) < (RX_TX_TSH/2)) &&
				((txBytesKBps + rxBytesKBps) < (0.8 * RX_TX_TSH)) ){ 
			/* only 25% slots have transfer + we may detect stop of streaming - lets slow down: defensive in ramp down */
			if(delayTimeMilliSec < SLOW_SAMPLE) delayTimeMilliSec +=FAST_SAMPLE;
		}

		
		if(sampleNum%(1 + 5)==0)
			Log.d("DetMet","mRx=" + df.format(mAvgRX) +" :mTX="+ df.format(mAvgTX)+
				" :NZSC="+Integer.toString(nonZeroSampleCount) + " :tx=" + txBytesKBps + 
				" :rx=" + rxBytesKBps+ " :Delay="+delayTimeMilliSec+ " : #Spd=" + 
				sIC.getCount()+" :#SAE=" +sAC.getCount());


		String text = "mRX="+ df.format(mAvgRX)+",mTX=" + df.format(mAvgTX);
		if(null!= log1)
			text += ":" + log1 + log2;

		
		if(null != locationManager){
			sIC.add(totBytes,get_network(),text,getCurrentLocation());
		} else {
			sIC.add(totBytes,get_network(), text);
		}
	}
	
	
	String get_network()
	{
		String network_type="UNKNOWN";//maybe usb reverse tethering
		NetworkInfo active_network=((ConnectivityManager)svcRef.getSystemService(TrackService.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (active_network!=null && active_network.isConnectedOrConnecting()) {
			if (active_network.getType()==ConnectivityManager.TYPE_WIFI) {
				network_type="WIFI";
			} else if (active_network.getType()==ConnectivityManager.TYPE_MOBILE) {
				network_type=((ConnectivityManager)svcRef.getSystemService(TrackService.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getSubtypeName();
			}
		}
		return network_type;
	}


	String saveLogs() 
	{
		FileOutputStream fileos=null;
		String currentFilePath=svcRef.folderPath+ svcRef.getDevIdentity()+"-" + LoadPreferences("createdFileIdx")+".xml";
		
		try {
			Log.d(MY_TAG,"logStreamCreating");
			fileos = new FileOutputStream(currentFilePath);
			int index = Integer.parseInt(LoadPreferences("createdFileIdx"))+1;
			SavePreferences("createdFileIdx",Integer.toString(index) );
			
		} catch (Exception e) {
			//svcRef.displaySrvcErrorNotification("Create XML File ",e);
			return null;
		}

		
		//we create a XmlSerializer in order to write xml data
		XmlSerializer serializer=Xml.newSerializer();
		try {
			serializer.setOutput(fileos, "UTF-8");
			serializer.startDocument(null, Boolean.valueOf(true)); 
			//set indentation option
			serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true); 
			//start a tag called "root"
			serializer.startTag(null, "Logfile"); 
	
			sAC.appendXmlEntries(serializer);
			sIC.appendXmlEntries(serializer);

			serializer.startTag(null, "other");
			String txt = Long.toString(SystemClock.elapsedRealtime()/1000) +" " +Long.toString(SystemClock.uptimeMillis()/1000);
			TelephonyManager tm = (TelephonyManager) svcRef.getSystemService(Context.TELEPHONY_SERVICE);
			CellLocation cl =  tm.getCellLocation();
			if(cl instanceof CdmaCellLocation) {
				CdmaCellLocation loc = (CdmaCellLocation)cl;
				txt += "lat:" + loc.getBaseStationLatitude () + "long:" + loc.getBaseStationLongitude() +
						"sys:" + loc.getSystemId() + "net:" + loc.getNetworkId() + "id:" + loc.getBaseStationId();
			}
			if(cl instanceof GsmCellLocation) {
				GsmCellLocation loc = (GsmCellLocation)cl;
				txt += ":lac=" + loc.getLac() + ":cid=" + loc.getCid() ;
			}  
			serializer.text(txt);
			serializer.endTag(null, "other");
	
			serializer.endTag(null, "Logfile");
			serializer.endDocument();
			serializer.flush();
			Log.d(MY_TAG,"logStreamCreating");
		} catch (Exception e) {
			svcRef.displaySrvcErrorNotification("XmlSerializer ",e);
		}
		try {
			fileos.close();
		} catch (Exception e) {
			svcRef.displaySrvcErrorNotification("file Close ",e);
		}
		
		serializer = null; // explicit release
		Log.d(MY_TAG, "file created successfully:"+currentFilePath);

		// reinitialize our structures / data points
		sAC = new ServiceAppCollector();
		sIC = new SpeedInfoCollector(sAC);
		
		return currentFilePath;
	}
	

	boolean shouldCreateLogFile() 	{
		// for testing NUMBER_NODES / 20
		return (sAC.getCount() + sIC.getCount() > NUMBER_NODES/5);
		// for real all NUMBER_NODES
		// return (sAC.getCount() + sIC.getCount() > NUMBER_NODES);
	}
	/**
	 * 
	 * @param batteryPct - current battery level
	 * @param isCharging - true -if the phone is currently charging.false otherwise
	 * @param usbCharge - true if the phone is charging using usb
	 * @param acCharge - true if the phone is charging using ac power
	 * @return the next poll delay
	 */
	
	int getNextPollDelay(double batteryPct,boolean isCharging,boolean usbCharge,boolean acCharge,int previous){
		int last_sample=previous;
		if(isCharging==true && acCharge==true)
		{   last_sample=FAST_SAMPLE;
			return last_sample;
		}
		else if(isCharging==false)
		{
		    double temp=(Math.pow(2.0, 4.0))*batteryPct;
		    double temp_val=(1/temp) * SLOW_SAMPLE;
		    int temp_sample=(int)temp_val;
		    last_sample=Math.max(temp_sample,last_sample);
		    return last_sample;
		}
		else if(isCharging==true && usbCharge==true)
		{  
			double alpha=0.5;//alpha value has to set on experimental basis
			double temp=(Math.pow(2.0, 4.0))*batteryPct;
		    double temp_val=(1/temp) * SLOW_SAMPLE*alpha;
		    int temp_sample=(int)temp_val;
		    int usb_sample=Math.min(last_sample,temp_sample);
		    last_sample=Math.max(FAST_SAMPLE, usb_sample);
		    return last_sample;
		    
		}
		
		// if battery above 80% FAST
		// between 80-60 (fast+slow)/2
		// below 60 SLOW
		//for testing - fast
		return SLOW_SAMPLE;
		// else return SLOW_SAMPLE;
	}
	
	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			String message = String.format(
					"New Location \n Longitude: %1$s \n Latitude: %2$s",
					location.getLongitude(), location.getLatitude()
					);
			Log.d(MY_TAG, message);
			// TBD add entry to XML log file
		}

		public void onStatusChanged(String s, int i, Bundle b) { /* do noting */ }

		public void onProviderDisabled(String s) {
			Log.w(MY_TAG,"Provider disabled by the user. GPS turned off");
		}

		public void onProviderEnabled(String s) {
			Log.d(MY_TAG, "Provider enabled by the user. GPS turned on"	);
		}

	}

	String getCurrentLocation() {
		if(null == locationManager)
			return "unknown";
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(null == location) location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			String latitude=Double.toString(location.getLatitude());
			String longitude=Double.toString(location.getLongitude());
			return (latitude + "," + longitude);
		} else {
			return "unknown";
		}
	}   

	
	private void SavePreferences(String key, String value){
		SharedPreferences sharedPreferences = svcRef.getSharedPreferences("MY_SHARED_PREF", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	private String LoadPreferences(String s){
		SharedPreferences sharedPreferences =  svcRef.getSharedPreferences("MY_SHARED_PREF", Context.MODE_PRIVATE);;
		String name = sharedPreferences.getString(s, "");
		return name;
	}


	
	class ServiceAppCollector {
		class SvcAppEntry {
			String nm;
			String time;
			Integer id;
			Boolean isService;
			Boolean foreGround = false;
			SvcAppEntry(String name, String date, int threadId, boolean service, boolean fg){
				nm = name; time = date; id = threadId; isService = service; foreGround = fg; 
			}
		}
		
		ArrayList<SvcAppEntry> entries = new ArrayList<SvcAppEntry>();

		HashMap<String, Integer> processMap= new HashMap<String, Integer>();

		void add(String name, int threadId, boolean service, boolean fg){
			if(entries.size()==0)
				addAll();
			else
				checkAndAddSpecific(name, threadId, service, fg);
		}

		void checkAndAddSpecific(String name, int threadId, boolean service, boolean foreGround){
			if(processMap.containsKey(name) && threadId == processMap.get(name))
				return; // duplicate
			processMap.put(name, threadId);
			entries.add(new SvcAppEntry(name, svcRef.getCurrDate(), threadId, service, foreGround));
		}

		void addAll() {
			List<RunningServiceInfo> servicesList=actMgr.getRunningServices(100);
			List<RunningAppProcessInfo> procInfos=actMgr.getRunningAppProcesses();
			for(int idx =0; idx <servicesList.size(); idx++){
				RunningServiceInfo rSvcInfo = servicesList.get(idx);
				String name = rSvcInfo.clientPackage;
				if(null == name) name = "Proc:" + rSvcInfo.process;
				checkAndAddSpecific(name, rSvcInfo.pid,true, rSvcInfo.foreground);
				
			}
			for(int idx =0; idx <procInfos.size(); idx++){
				RunningAppProcessInfo rAppInfo = procInfos.get(idx);
				// not easy to know if it has focus or is in foreground - avoid CPU intensive check
				checkAndAddSpecific(rAppInfo.processName, rAppInfo.pid,false, false); 
			}
		}
		int getCount() {
			return entries.size();
		}
		
		void appendXmlEntries(XmlSerializer xSer) throws IOException{
			for(int idx =0; idx < entries.size(); idx++)
			{
				SvcAppEntry sAE = entries.get(idx);
				xSer.startTag(null, "process");
				if(sAE.isService) xSer.attribute(xSer.getNamespace(), "svc", "1");
				if(sAE.foreGround) xSer.attribute(xSer.getNamespace(), "fg", "1");
				xSer.attribute(xSer.getNamespace(), "ts",sAE.time);						
				xSer.attribute(xSer.getNamespace(), "pid",Integer.toString(sAE.id));						
				xSer.text(sAE.nm);
				xSer.endTag(null, "process");
			}
		}
	}
	
	class SpeedInfoCollector {
		class SpeedEntry {
			long statCount;
			String text;
			String time;
			String location;
			String operator;

			SpeedEntry(long stats, String txt, String opr, String date, String loc){
				statCount = stats; text = txt; operator = opr; 
				time = date; location = loc;  
			}
		}
		ServiceAppCollector sAC = null;
		ArrayList<SpeedEntry> entries = new ArrayList<SpeedEntry>();

		SpeedInfoCollector(ServiceAppCollector aAC){
			sAC = aAC;
		}
		
		void add(long statCount, String netOperator, String metricsDump, String location){
			RunningTaskInfo rTI = actMgr.getRunningTasks(1).get(0);
			String packName = rTI.topActivity.getPackageName();
			sAC.add(packName, rTI.id, false, true);
			metricsDump+=":" + packName;
			entries.add(new SpeedEntry(statCount,metricsDump, netOperator, svcRef.getCurrDate(),location));
		}

		void add(long statCount, String netOperator, String metricsDump){
			add(statCount,netOperator, metricsDump, null);
		}
		
		void appendXmlEntries(XmlSerializer xSer) throws IOException{
			for(int idx =0; idx < entries.size(); idx++)
			{
				SpeedEntry sE = entries.get(idx);
				xSer.startTag(null, "speed");
				xSer.attribute(xSer.getNamespace(), "ts",sE.time);						
				xSer.attribute(xSer.getNamespace(), "op",sE.operator);						
				xSer.attribute(xSer.getNamespace(), "stat",Long.toString(sE.statCount));						
				if(null != sE.location) xSer.attribute(xSer.getNamespace(), "loc", sE.location);
				xSer.text(sE.text);
				xSer.endTag(null, "speed");
			}
			
		}
		
		int getCount() {
			return entries.size();
		}

	}
}
