package in.swifiic.plat.app.suta.andi.mgmt;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
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

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

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
    public void onCreate() {
        Toast.makeText(getApplicationContext(), "Trace service starting", Toast.LENGTH_LONG).show();
        Log.d("TraceService", "GO!");

//        String filename = "traceDataFile";
//        File file = new File(getApplicationContext().getFilesDir(), filename);
//        file.delete();
//        Log.d("TraceService", "file deleted");

        getLocation(LocationManager.NETWORK_PROVIDER);

        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
//        int batteryLevel = getBatteryLevel();
//        Location location;
//        if (batteryLevel > 75) {
//            location = getLocation(LocationManager.GPS_PROVIDER);
//        } else {
//            location = getLocation(LocationManager.NETWORK_PROVIDER);
//        }
////        Toast.makeText(getApplicationContext(), "Latitude" + location.getLatitude() + "Longitude" + location.getLongitude() + "Battery%" + batteryLevel, Toast.LENGTH_SHORT).show();
//        if (location != null) {
////            Log.d("TraceService", "Latitude" + location.getLatitude() + "Longitude" + location.getLongitude() + "Battery%" + batteryLevel);
//        Toast.makeText(getApplicationContext(), "Latitude" + location.getLatitude() + "Longitude" + location.getLongitude() + "Battery%" + batteryLevel, Toast.LENGTH_SHORT).show();
//
//        }
    }

    private Location getLocation(String locationProvider) {
        Location location;
        try {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(locationProvider, MIN_TIME_DELTA, MIN_DISTANCE_DELTA, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(Calendar.getInstance().getTime());

                    int batteryLevel = getBatteryLevel();
//                    Log.d("TraceService", "Latitude" + location.getLatitude() + "Longitude" + location.getLongitude() + "Battery%" + batteryLevel);
                    String filename = "traceDataFile";
                    File file = new File(getApplicationContext().getFilesDir(), filename);
                    String traceData = formattedDate + "|" + location.getLatitude() + "|" + location.getLongitude() + "|" + batteryLevel + "\n";
                    try {
                        FileOutputStream outputStream = new FileOutputStream(file, true);
//                        outputStream = openFileOutput(file, Context.MODE_PRIVATE);
                        outputStream.write(traceData.getBytes());
                        outputStream.close();
                        Log.d("TraceService", "DONE");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


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
