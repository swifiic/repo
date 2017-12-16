package in.swifiic.plat.helper.andi;

import in.swifiic.plat.helper.andi.GenericService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class IBRDtnReceiver extends BroadcastReceiver {
	//protected Class<IntentService> className = null; 
	static final String TAG = "IBRDtnReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive:" + action);
        if (action.equals(Constants.IBR_DTN_RECEIVE))
        {
            // We received a notification about a new bundle and
            // wake-up the local MsngrService to received the bundle.
            Intent i = new Intent(context, GenericService.class);
            i.setAction(Constants.IBR_DTN_RECEIVE); // code hit 14 Dec XXX TBD
            context.startService(i);
        }
        else if (action.equals(Constants.IBR_DTN_STATUS_REPORT))
        {
            // We received a status report about a bundle and
            // wake-up the local MsngrService to process this report.
            Intent i = new Intent(context, GenericService.class);
            i.setAction(Constants.REPORT_DELIVERED_INTENT);
            i.putExtra("source", intent.getParcelableExtra("source"));
            i.putExtra("bundleid", intent.getParcelableExtra("bundleid"));
            context.startService(i);
        }
    }
}