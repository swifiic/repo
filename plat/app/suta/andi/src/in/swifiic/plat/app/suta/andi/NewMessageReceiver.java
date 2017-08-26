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
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NewMessageReceiver extends BroadcastReceiver {

	private static Context mcontext = null;
	public NewMessageReceiver(){
	}

	private static final String TAG = "NewMessageReceiver";
	int lastReceivedSeqNo;
	public static SharedPreferences pref = null; 

	@Override
	public void onReceive(Context context, Intent intent) {
		if(null == NewMessageReceiver.pref) {
			mcontext = context;
			NewMessageReceiver.pref = PreferenceManager.getDefaultSharedPreferences(context);
		}
		if (intent.hasExtra("notification")) {
        	String payload = intent.getStringExtra("notification");
            Log.d(TAG, "Handling incoming messages: " + payload);
            Notification notif = Helper.parseNotification(payload);
            handleNotification(notif);
        } else {
            Log.d(TAG, "Broadcast Receiver ignoring message - no notification found");
        }
	}
	
	protected void handleNotification(Notification notif){
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
    	
    	if(null == Provider.providerInstance){
    		Log.e(TAG,"No Provider - whatsup...?");
    	} else {
    		if(notif.getNotificationName().equals("DeviceListUpdate")) {
				WifiManager wimanager = (WifiManager) mcontext.getSystemService(Context.WIFI_SERVICE);
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

					Provider.providerInstance.loadUserSchema(userList);
					Provider.providerInstance.storeAccountDetails(accountDetails, macAddress, notifSentByHubAt,notifRecievedBySutaAt);

				}
    			else if(seqno > lastReceivedSeqNo){

    				lastReceivedSeqNo=seqno;
    				Log.d(TAG, "Got user list as: " + userList);
					Log.d(TAG, "Got user accountDetails as: " + accountDetails);

					Provider.providerInstance.loadUserSchema(userList);
					Provider.providerInstance.storeAccountDetails(accountDetails, macAddress, notifSentByHubAt,notifRecievedBySutaAt);

    		}
    			else
    			{
    				Log.e(TAG,"Out dated Notification discarding it");
    			}
    		}
    	}
    }

}
