package in.swifiic.plat.app.suta.andi.mgmt;




import in.swifiic.plat.app.suta.andi.R;
import in.swifiic.plat.helper.andi.AppEndpointContext;
import in.swifiic.plat.helper.andi.Helper;
import in.swifiic.plat.helper.andi.xml.Action;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.acl.LastOwnerException;
import java.text.DateFormat;
import java.util.List;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import android.net.NetworkInfo;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import android.net.ConnectivityManager;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import in.swifiic.plat.helper.andi.xml.Action;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TrackService extends Service {


	private Handler mHandler = new Handler();
	 private AppEndpointContext aeCtx = new AppEndpointContext("suta", "0.1", "1");
		
	String devIdentity = null;
	static TAContext ctx = null;
	static final String MY_TAG="TrackSrvc";
	static final String excpFileName = "ErrorFile.txt"; 

    String folderPath = null;
    IntentFilter ifilter=null;
    Intent batteryStatus=null;
    double batteryPct=0.0;
    int previous=2000;
    int last_sample=2000;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(MY_TAG,"Service STARTED");
	    String packageName = this.getPackageName().trim();
	    folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + 
	    								"/Android/data/" + packageName + "/files/";
	 ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	 batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
	 SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
     pref=PreferenceManager.getDefaultSharedPreferences(this);
 	
	
      String hubAddress = pref.getString("hub_address", "dtn://aarthi-hub.dtn");
   //  Helper.sendSutaInfo(act, hubAddress + "/suta", this);

		try {
		    File folder  = new File(folderPath);
		    boolean exists = folder.exists();
		    if (!exists) 
		        folder.mkdirs();
		    File exptFile = new File(folderPath + excpFileName);
		    if(exptFile.length()>0){
		    	exptFile.renameTo(new File (folderPath + excpFileName + ".old"));
		    	Log.w(MY_TAG, "Old exception log file renamed");
		    }
		} catch (Exception e){
			Toast.makeText(this, "Folder Create" + e.getMessage(), Toast.LENGTH_LONG).show();
			Log.e(MY_TAG, "Folder Create" + e.getMessage());
		}
		ctx = TAContext.createContextFromPersistedData(this);
		mHandler.postDelayed(mRunnable, 10000);
	}

	@Override
	public void onDestroy() {
		mHandler.removeCallbacks(mRunnable);
		ctx.saveLogs();
		ctx = null; // explicit free - may not be needed
		super.onDestroy();
		Log.d(MY_TAG, "Service Destroyed");
	}

	private int errNotifyId = 2;
	public void displaySrvcErrorNotification(String msg, Exception e)
	{
		NotificationManager notificationManager;
		Notification myNotification;
		final String notificationTitle = "TrackAppExcpt";
		StackTraceElement stElArr[] = e.getStackTrace();
		String text = msg + e.getMessage() +"\n";
		for(int i=0; i < stElArr.length; i++){
			text += "\t" + stElArr[i].getFileName() + ":" + stElArr[i].getMethodName() +
					":" + stElArr[i].getLineNumber() + "\n"; 
		}
		
	    Intent intent = new Intent(this, ErrorNotifyActivity.class);
	    intent.putExtra("text", text);
	    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
	    

		Log.e(MY_TAG, text);
		notificationManager =(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder notifBuild = new Notification.Builder(getBaseContext());
		notifBuild.setContentTitle(notificationTitle);
		//notifBuild.setContentText(text);
		notifBuild.setContentText(text);
		notifBuild.setSmallIcon(R.drawable.icon);
		notifBuild.setContentIntent(pIntent);
		
		
		//notifBuild.addAction(R.drawable.icon,"More",pIntent);
		
		myNotification = notifBuild.getNotification(); // deprecated by build in API 16 and above
		myNotification.defaults |= Notification.DEFAULT_SOUND;
		myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		notificationManager.notify(errNotifyId++, myNotification);

		if(errNotifyId > 3) errNotifyId = 3; // do not cause too many notifications
		appendToExternalStorageErrFile(text);
		
	}



	 String appendToExternalStorageErrFile(String msg) {
		    File                file            = null;
		    FileOutputStream    fOut            = null;

		    try {
		        try {
		                file = new File(folderPath + excpFileName);
		                if (file != null && file.length() < (1024 * 16)) {
		                    fOut = new FileOutputStream(file,true);
		                    if (fOut != null) {
		                        fOut.write(msg.getBytes());
		                    }
		                }
		        } catch (Exception e) {
		            Toast.makeText(this, "Append to Error File" + e.getMessage(), Toast.LENGTH_LONG).show();
		        }
		        return file.getAbsolutePath();
		    } finally {
		        if (fOut != null) {
		            try {
		                fOut.flush();
		                fOut.close();
		            } catch (Exception e) {
		                Toast.makeText(this, "Append To Log File" + e.getMessage(), Toast.LENGTH_LONG).show();
		            }
		        }
		    }
		}
	 
	public String getCurrDate()	{
		Date cal = Calendar.getInstance().getTime();
		return DateFormat.getDateTimeInstance().format(cal); 
	}


	public String getDevIdentity(){
		return devIdentity;
	}
	

	public void getDeviceName() {
		String idIMEI=null;
		String model=null;

		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		idIMEI =  tm.getDeviceId();
		if (idIMEI == null) idIMEI = "-srl-" + android.os.Build.SERIAL;
		model = Build.MODEL +Build.MANUFACTURER;
		devIdentity = idIMEI + model;
		devIdentity = devIdentity.trim();
	} 

	private final Runnable mRunnable = new Runnable() {
		private String version=null;
		


		public boolean isAndroidEmulator() {
			String model = Build.MODEL;
			String product = Build.PRODUCT;
			boolean isEmulator = false;
			if (product != null) {
				isEmulator = product.equals("sdk") || product.contains("_sdk") || product.contains("sdk_");
			}
			if(model != null) {
				isEmulator |= model.contains("sdk");
			}
			if(devIdentity.contains("0000000000")) isEmulator = true;
			return isEmulator;
		}

		private boolean isWifi()
		{
			NetworkInfo active_network=((ConnectivityManager)getSystemService(TrackService.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			if (active_network!=null && active_network.isConnectedOrConnecting())
			{	
				if (active_network.getType()==ConnectivityManager.TYPE_WIFI)
					return true;
			}
			return isAndroidEmulator();
		}


		void initData(){
			getDeviceName();
			try {
				String pkg = getPackageName();
				version = getPackageManager().getPackageInfo(pkg, 0).versionName;
			} catch (NameNotFoundException e) {
				version = "?";
			}
		}

		public void run() {
			if(devIdentity==null) {
				initData();
				// recover the serialized context
			}
			 ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			 batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
			 int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			 int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			 int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			 boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
			                      status == BatteryManager.BATTERY_STATUS_FULL;

			 // How are we charging?
			 int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			 boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
			 boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
			 

			 batteryPct = level / (float)scale;
			ctx.doSampling();

			if(ctx.shouldCreateLogFile()){
				String fileName = ctx.saveLogs();
				if (null != fileName)
					handleLogStreamEndAndFileUpload(fileName);
			}
			
		
			last_sample=ctx.getNextPollDelay(batteryPct, isCharging,usbCharge,acCharge,previous);
			 previous=last_sample;

			mHandler.postDelayed(mRunnable,last_sample);
		}

		
		private void SavePreferences(String key, String value){
			SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(key, value);
			editor.commit();
		}

		private String LoadPreferences(String s){
			SharedPreferences sharedPreferences =  getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);;
			String name = sharedPreferences.getString(s, "");
			return name;
		}

		void zip(String fileName) {
			final int BUFFER_SIZE = 1024;
			BufferedInputStream origin = null;
			ZipOutputStream out = null;
			
			try {
				out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileName+".zip")));
				byte data[] = new byte[BUFFER_SIZE];

				FileInputStream fi = new FileInputStream(fileName);    
				origin = new BufferedInputStream(fi, BUFFER_SIZE);
				try {
					ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1));
					out.putNextEntry(entry);
					int count;
					while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
						out.write(data, 0, count);
					}
				} catch(Exception e){
					displaySrvcErrorNotification("FizeZIP Entry:"+fileName,e);
				} finally {
					origin.close();
				}
			}catch(Exception e){
				displaySrvcErrorNotification("FizeZIP Out:"+fileName,e);
			}finally {
				try {
					if(null != out) out.close();
				} catch(Exception e){
					displaySrvcErrorNotification("FizeZIP Close:"+fileName,e);
				}
			}
			File file = new File(fileName);
			Log.d("ZIP", "removing unzipped file " + fileName + "of size" + file.length());
			file.delete();
		}
	
		void handleLogStreamEndAndFileUpload(String currentFilePath)
		{

			String str = LoadPreferences("fileuploads");
			int lastuploadwifi =Integer.parseInt(str);
			str = LoadPreferences("createdFileIdx");
			int currUploadIndex  = Integer.parseInt(str);

			if(isWifi()) // TODO check if IBR DTN daemon is connected
			{
				Log.d(MY_TAG, "detected wifi/emulator");
				lastuploadwifi=Integer.parseInt(LoadPreferences("fileuploads").toString());
				for(int i=lastuploadwifi;i<currUploadIndex;i++)
				{
					// compress the file currentFilePath
					String xmlFileName = devIdentity+"-" + i +".xml";
					String localFileName =folderPath +xmlFileName;
					File file=new File(localFileName);
					if(file.length()>100) { // XML exists - zip it 
						//zip(localFileName);
						//String fileName = devIdentity+ "-" +i +".xml.zip";
						//localFileName = folderPath+fileName;
						//Log.d(MY_TAG, "file zipped to:"+currentFilePath+".zip");
						String base64str=Helper.fileToB64String(localFileName);
						Log.d(MY_TAG,"File converted to 64 bit string"+base64str.substring(0, 100));
						dtnSendFile(base64str,xmlFileName);
						file.delete();
					}
				}
			}
			else
			{ 
				Toast.makeText(getApplicationContext(), "wifinotdetected", Toast.LENGTH_SHORT).show();
				Log.w(MY_TAG, "NO WiFi detected");
			}	
		}};


		
		
void dtnSendFile(String message,String filename)
{
	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
	Action act = new Action("SendMessage", aeCtx);
   /* WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    WifiInfo info = manager.getConnectionInfo();
    String macAddress = info.getMacAddress();
    act.addArgument("macAddress", macAddress);
    Calendar c = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
    String strDate = sdf.format(c.getTime());
    act.addArgument("dateTime",strDate);*/
	Date date = new Date();
	String sentAt = "" + date.getTime();
    
	
    act.addArgument("message", message);
    act.addArgument("filename",filename);
    act.addArgument("sentAt", "" + sentAt);  

    
    // Loading hub address from preferences
    String hubAddress = sharedPref.getString("hub_address", "");
    Log.d(MY_TAG,"Hub address:::::"+hubAddress);
   
    if(hubAddress.equals(""))
    	hubAddress="dtn://aarthi-hub.dtn";
    act.addArgument("sentTo",hubAddress+"/suta");
    Helper.sendAction(act, hubAddress + "/suta", this);
}
}
