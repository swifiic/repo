package in.swifiic.plat.helper.andi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class HubAddressReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String hubAddress = intent.getStringExtra("hubAddress");
		String myIdentity = intent.getStringExtra("identity");
		String resetvalue=	intent.getStringExtra("resetrequired");
	    sharedPref.edit().putString("hub_address", hubAddress).commit();
        sharedPref.edit().putString("my_identity", myIdentity).commit();
        sharedPref.edit().putString("reset_required",resetvalue).commit();
       
        Log.d("HubAddressReceiver", "Broadcast update : " + hubAddress+";"+myIdentity+";"+resetvalue);
       // Log.d("HubAddressReceiver","Broadcast update reset received with hub: " + resetvalue);
	}
}
