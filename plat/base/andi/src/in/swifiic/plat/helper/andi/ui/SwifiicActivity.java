package in.swifiic.plat.helper.andi.ui;

import in.swifiic.plat.helper.andi.GenericService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;


public class SwifiicActivity extends Activity {
	protected GenericService mService = null;
    private boolean mBound = false;
    
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (!mBound) {
            // bind to the MsngrService
            bindService(new Intent(this, GenericService.class), mConnection, Context.BIND_AUTO_CREATE);
            mBound = true;
        }
    }
    
	@Override
	protected void onDestroy() {
        // unbind from service
        if (mBound) {
            // unbind from the MsngrService
            unbindService(mConnection);
            mBound = false;
        }       
		super.onDestroy();
	}

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((GenericService.LocalBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };	
}
