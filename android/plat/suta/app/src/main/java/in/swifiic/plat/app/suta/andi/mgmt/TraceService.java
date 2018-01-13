package in.swifiic.plat.app.suta.andi.mgmt;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

//import com.commonsware.cwac.wakeful.WakefulIntentService;

import in.swifiic.plat.helper.andi.GenericService;

/**
 * Created by nic on 12/1/18.
 */

public class TraceService extends IntentService {
    private Messenger messageHandler;
    private static final long MIN_DISTANCE_DELTA = 0;//10;
    private static final long MIN_TIME_DELTA = 0;//1000*60*1;
    public TraceService() {
        super("Service started");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Trace service starting", Toast.LENGTH_LONG).show();

        Bundle extras = intent.getExtras();
        messageHandler = (Messenger) extras.get("MESSENGER");
        Message msg = new Message();
        msg.arg1 = 24;

        try {
            messageHandler.send(msg);
            Log.d("TraceService", "Msg sent!");

        } catch (RemoteException re) {
            Log.e("TraceService", "Couldn't send to main activity");
        }

        getLocation(LocationManager.NETWORK_PROVIDER);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int batteryLevel = getBatteryLevel();
        Location location;
        if (batteryLevel > 75) {
            location = getLocation(LocationManager.GPS_PROVIDER);
        } else {
            location = getLocation(LocationManager.NETWORK_PROVIDER);
        }
//        Toast.makeText(getApplicationContext(), "Latitude" + location.getLatitude() + "Longitude" + location.getLongitude() + "Battery%" + batteryLevel, Toast.LENGTH_SHORT).show();
        if (location != null) {
            Log.d("TraceService", "Latitude" + location.getLatitude() + "Longitude" + location.getLongitude() + "Battery%" + batteryLevel);
        }
    }

    private Location getLocation(String locationProvider) {
        Location location;
        try {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(locationProvider, MIN_TIME_DELTA, MIN_DISTANCE_DELTA, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
            location = locationManager.getLastKnownLocation(locationProvider);

        } catch (SecurityException e) {
            Log.d("TraceService", "We probably didn't get the permission");
            location = null;
        }
        return location;
    }

    private int getBatteryLevel() {
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        int batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return batteryLevel;
    }

}
