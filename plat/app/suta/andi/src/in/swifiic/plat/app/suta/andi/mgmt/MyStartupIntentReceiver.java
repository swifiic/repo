package in.swifiic.plat.app.suta.andi.mgmt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyStartupIntentReceiver extends BroadcastReceiver{
@Override
public void onReceive(Context context, Intent intent) {
	Intent serviceIntent = new Intent();
	serviceIntent.setAction("in.swifiic.plat.app.suta.andi.mgmt.TrackService");
	context.startService(serviceIntent);
}
}