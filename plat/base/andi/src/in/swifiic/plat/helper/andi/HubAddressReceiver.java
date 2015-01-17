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
        sharedPref.edit().putString("hub_address", hubAddress).commit();
        Log.d("HubAddressReceiver", "Broadcast update hub address received with hub: " + hubAddress);
	}
}
