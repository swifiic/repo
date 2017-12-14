package in.swifiic.plat.app.suta.andi;

import in.swifiic.plat.app.suta.andi.provider.Provider;
import in.swifiic.plat.helper.andi.Helper;
import in.swifiic.plat.helper.andi.xml.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.tubs.ibr.dtn.util.Base64;

public class NewMessageReceiver extends BroadcastReceiver {

	public NewMessageReceiver(){
	}

	private static final String TAG = "NewMessageReceiver";
	int lastReceivedSeqNo;
	//public static SharedPreferences pref = null;

	@Override
	public void onReceive(Context context, Intent intent) { //2ASK: maybe this should be async task
		if (intent.hasExtra("filename")) { // Received as a response to RequestApp
			String filename = intent.getStringExtra("filename");
			try {
				File file = context.getFileStreamPath(filename);
				byte[] bytes = new byte[(int) file.length()];
				// Read the Notification from a file and give it to handleNotification for processing
				InputStream inputStream = new FileInputStream(file);
				try {
					inputStream.read(bytes);
					String payload = new String(bytes);
					Notification notif = Helper.parseNotification(payload);
					Log.d(TAG, "good!");
					handleNotification(context, notif);
				} finally {
					inputStream.close();
				}
			} catch (Exception e) {
				Log.d(TAG, "File not found!");
			}
		} else if (intent.hasExtra("notification")) {
        	String payload = intent.getStringExtra("notification");
            Log.d(TAG, "Handling incoming messages: " + payload);
            Notification notif = Helper.parseNotification(payload);
            handleNotification(context, notif);
            Log.d(TAG, "ReceiveOpName " + notif.getNotificationName());
        } else {
            Log.d(TAG, "Broadcast Receiver ignoring message - no notification found");
        }
	}
	
	protected void handleNotification(Context context, Notification notif){
    	// TODO
    	// we need these arguments

    	// lastSyncToHubReceivedAtHub
    	// lastMessageReceivedAtHub
    	// lastBillingDetails - free string ...
    	// balance / credit
    	// userAppListLastChangeTimestamp on appHub
    	
    	// userList <u name="" id="" alias="" />...
    	// appList  <app name="" id="" alias=""> <role alias=""> <u id="" /> ... </role> <role...> </app>
    	
    	// Above was the grand goal - now simple implementation
    	// userList "username|alias;username|alias;..."
    	// appList  "appName|appId|appAlias|role1Alias:usrId:usrId:usrId|role2alias:...;
    	
    	if(null == Provider.getProviderInstance()){
    		Log.e(TAG,"No Provider - whatsup...?");
    	} else {
    		String opName = notif.getNotificationName();
    		if(opName.equals("DeviceListUpdate")) {
				WifiManager wimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				String macAddress = wimanager.getConnectionInfo().getMacAddress();

				Calendar c = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String notifRecievedBySutaAt = sdf.format(c.getTime());
				// suta got notification at this time

				String userList = notif.getArgument("userList");
				String accountDetails = notif.getArgument("accountDetails");
    			String notifSentByHubAt =notif.getArgument("currentTime");
				// currTime is time at which Hub sends the notification

    			int seqno=Integer.parseInt(notif.getArgument("sequenceNumber"));
    			if(seqno==1)
    			{
    				lastReceivedSeqNo=seqno;
    				Log.d(TAG, "Got user list as: " + userList);
					Log.d(TAG, "Got user accountDetails as: " + accountDetails);

					Provider.getProviderInstance().loadUserSchema(userList);
					Provider.getProviderInstance().storeAccountDetails(accountDetails, macAddress, notifSentByHubAt,notifRecievedBySutaAt);

				}
    			else if(seqno > lastReceivedSeqNo) {

					lastReceivedSeqNo = seqno;
					Log.d(TAG, "Got user list as: " + userList);
					Log.d(TAG, "Got user accountDetails as: " + accountDetails);

					Provider.getProviderInstance().loadUserSchema(userList);
					Provider.getProviderInstance().storeAccountDetails(accountDetails, macAddress, notifSentByHubAt, notifRecievedBySutaAt);
				} else {
						Log.e(TAG,"Out dated Notification discarding it");
				}
			} else if(opName.equals("SendAPKMessage")) {
    			// Write the APK to a file and alert the user about the same in a Toast
				String encodedApk = notif.getArgument("encodedApk");
				String appFileName = notif.getArgument("appFileName");
				Log.d(TAG, encodedApk);
				String filename = appFileName + ".apk";
				try {
					byte[] decodedApk = Base64.decode(encodedApk);
					File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					fileOutputStream.write(decodedApk);
					fileOutputStream.close();
					Toast.makeText(context, "APK saved to Downloads folder", Toast.LENGTH_LONG).show();
					Log.d(TAG, "File Saved!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
}
