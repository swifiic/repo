package in.swifiic.plat.helper.andi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class IdentityReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String myIdentity = intent.getStringExtra("myIdentity");
        sharedPref.edit().putString("my_identity", myIdentity).commit();
        Log.d("IdentityReceiver", "Broadcast update for my identity received with identity: " + myIdentity);
	}
}
