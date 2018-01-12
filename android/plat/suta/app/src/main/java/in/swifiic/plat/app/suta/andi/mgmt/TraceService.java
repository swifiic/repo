package in.swifiic.plat.app.suta.andi.mgmt;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by nic on 12/1/18.
 */

public class TraceService extends IntentService {

    public TraceService() {
        super("Service started");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Trace service starting", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
