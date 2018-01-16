package in.swifiic.plat.app.suta.andi;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import in.swifiic.plat.app.suta.andi.mgmt.TraceService;
import in.swifiic.plat.app.suta.andi.mgmt.TrackService;
import in.swifiic.plat.helper.andi.ui.SwifiicActivity;
import in.swifiic.plat.helper.andi.ui.UserChooserActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.design.widget.TabLayout;

import org.apache.commons.io.IOUtils;

import in.swifiic.plat.helper.andi.xml.Action;
import in.swifiic.plat.helper.andi.AppEndpointContext;
import in.swifiic.plat.helper.andi.Helper;

public class MainActivity extends SwifiicActivity implements StatusFragment.OnFragmentInteractionListener, AppList.OnFragmentInteractionListener {


    @SuppressWarnings("unused")
    private final long timeDiff = 1*(60*1000); // in milli seconds
	private final String TAG="MainActivity";
    private AppEndpointContext aeCtx = new AppEndpointContext("suta", "0.1", "1");
    SimpleFragmentPagerAdapter mSimpleFragmentPagerAdapter;
    ArrayList<AppListData> mAppsList = new ArrayList<>();


//    private TextView remainingCredit,currTime,transactions;

    // Creates an Action for requesting an APK and sends it to the Hub.
    private void sendAppRequest(String appRequested) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String hubAddress = sharedPreferences.getString("hub_address", "");
        String fromUser = sharedPreferences.getString("my_identity", "");

        Date date = new Date();
        String epochDelta = String.valueOf(date.getTime());

        Action action = new Action("RequestApp", new AppEndpointContext("SUTA", "0.1", "1"));
        action.addArgument("appRequested", appRequested);
        action.addArgument("fromUser", fromUser);
        action.addArgument("toUser", hubAddress);
        action.addArgument("sentAt", epochDelta);
        Helper.sendAction(action, hubAddress + "/suta", getApplicationContext());
    }

    private String getWifiNetworksList() {
        StringBuilder stringBuilder = new StringBuilder();
        if (mScanResults != null) {
            for (ScanResult scanResult : mScanResults) {
                stringBuilder.append(scanResult.SSID);
                stringBuilder.append("|");
            }
            return stringBuilder.toString();
        }
        return "";
    }


    private void sendTraceData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String hubAddress = sharedPreferences.getString("hub_address", "");
        Date date = new Date();
        String epochDelta = String.valueOf(date.getTime());

        Action action = new Action("TraceDataDump", new AppEndpointContext("SUTA", "0.1", "1"));
        String traceData = readTraceData();
        String wifiList = getWifiNetworksList();
        if (traceData != null && wifiList != null) {
            action.addArgument("traceData", traceData);
            action.addArgument("wifiList", wifiList);
            Helper.sendAction(action, hubAddress + "/suta", getApplicationContext());
            Log.d("SUTA", "Sending TraceData message");
//            String filename = "traceDataFile";
//            File file = new File(getApplicationContext().getFilesDir(), filename);
//            file.delete();
        }
    }

    private boolean setStatusFragment(String creditValue, String lastUpdateTime) {
        Fragment fragment = mSimpleFragmentPagerAdapter.getFragment(0);
        if (fragment != null) {
            ((StatusFragment) fragment).setCredit(creditValue);
            ((StatusFragment) fragment).setLastUpdate(lastUpdateTime);
            return true;
        } else {
            Log.d("SUTA", "NULLNULLNULL");
            return false;
        }
    }

    private boolean setStatusFragment(String creditValue, String lastHubUpdateTime, String multicastReceiveTime, String lastAndroidUpdate) {
        Fragment fragment = mSimpleFragmentPagerAdapter.getFragment(0);
        if (fragment != null) {
            ((StatusFragment) fragment).setCredit(creditValue);
            ((StatusFragment) fragment).setLastUpdate(lastHubUpdateTime, multicastReceiveTime, lastAndroidUpdate);
            return true;
        } else {
            Log.d("SUTA", "NULLNULLNULL");
            return false;
        }
    }


    private boolean setupAppsList() {
        Fragment fragment = mSimpleFragmentPagerAdapter.getFragment(1);
        if (fragment != null) {
            ((AppList)fragment).setupListView(mAppsList);
            return true;
        } else {
            Log.d("SUTA", "NULLNULLNULL");
            return false;
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                mScanResults = mWifiManager.getScanResults();
            } else {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                setStatusFragment(pref.getString("remainingCredit", "Waiting for Hub"),
                                    pref.getString("notifSentByHubAt", "N/A"), //time at which hub sent msg
                                    pref.getString("notifRecievedBySutaAt","N/A"), //time at which android received hub mc
                                    pref.getString("androidTimestamp", "N/A"));

                Bundle extras = intent.getExtras();

                Log.d("SUTA", "APPIDS" + extras.getString("appIDs"));
                Log.d("SUTA", "APPNAMES" + extras.getString("appNames"));
                Log.d("SUTA", "APPDESC" + extras.getString("appDescriptions"));

                String[] appIDs = extras.getString("appIDs").split("\\|");
                String[] appNames = extras.getString("appNames").split("\\|");
                String[] appDescriptions = extras.getString("appDescriptions").split("\\|");

                for (int i = 0; i < appIDs.length; i++) {
                    addAppToList(appNames[i], appDescriptions[i], null);
                }
            }
        }
    };

    private WifiManager mWifiManager;
    private List<ScanResult> mScanResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        registerReceiver(broadcastReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mWifiManager.startScan();

        registerReceiver(broadcastReceiver, new IntentFilter("SUTA_APP_LIST_UPDATE"));

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        mSimpleFragmentPagerAdapter = new SimpleFragmentPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(mSimpleFragmentPagerAdapter);

//        mAppsList.add(new AppListData("Msngr", "A message sending app.", null));
//        mAppsList.add(new AppListData("Bromide", "An image sending app.", null));

        // getting the current time and checking the difference from last updated time
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String scurTime = sdf.format(c.getTime());

        String slastUpdatedTime = pref.getString("lastUpdatedTime","0");

        if (slastUpdatedTime.equals("0")){
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("lastUpdatedTime",scurTime);
            editor.commit();
            slastUpdatedTime = scurTime;
        }

        Date curDate = new Date();
        Date lastDate = new Date();
        try {
            curDate = sdf.parse(scurTime);
            lastDate = sdf.parse(slastUpdatedTime);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // diff will be in milli seconds
        long diff = curDate.getTime()-lastDate.getTime();

        Log.d(TAG,"diff = "+diff);
        // diff  will be zero for the first time
        if (diff > timeDiff || (diff == 0)) {
            sendInfoToHub();
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("lastUpdatedTime",scurTime);
            editor.commit();
        }

//        transactions = (TextView)findViewById(R.id.transactions);
//        transactions.setMovementMethod(new ScrollingMovementMethod());

//        transactions.setText(pref.getString("revisedTransactionDetails","waiting"));w


        // implement transaction history
        final TabLayout tabLayout = (TabLayout)findViewById(R.id.sliding_tabs);
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
                tabLayout.getTabAt(0);
                tabLayout.getTabAt(1);
                setStatusFragment(pref.getString("remainingCredit", "Waiting for Hub"),
                        pref.getString("notifSentByHubAt", "N/A"),
                        pref.getString("hubTimestamp", "N/A"),
                        pref.getString("androidTimestamp", "N/A"));
                setupAppsList();
            }
        });

        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);



        Intent serviceIntent = new Intent(this, TrackService.class);
        serviceIntent.setAction("in.swifiic.plat.app.suta.andi.mgmt.TrackService");
        this.startService(serviceIntent);
//        starts trace service class
//        Intent traceServiceIntent = new Intent(this, TraceService.class);
//        traceServiceIntent.putExtra("MESSENGER", new Messenger(messageHandler));
//        this.startService(traceServiceIntent);


//        final Handler handler = new Handler();
//        final int delay = 1000*60*5; //milliseconds
//
//        handler.postDelayed(new Runnable(){
//            public void run(){
//                sendTraceData();
//                handler.postDelayed(this, delay);
//            }
//        }, delay);
    }

    private String readTraceData() {
        String fileString = null;
        try {
            String filename = "traceDataFile";
            File file = new File(getApplicationContext().getFilesDir(), filename);
            FileInputStream inputStream = new FileInputStream(file);
            fileString = IOUtils.toString(inputStream, "UTF-8");
            Log.d("SUTA", "DATADATA" + fileString);
            inputStream.close();
        } catch (Exception e) {
            Log.e("SUTA", "File not found for telemtery");
        }
        return fileString;
    }

    public void onResume() //why do we change all the strings to waiting onresume?
    {
//        setupAppsList();
//        transactions.setText(pref.getString("revisedTransactionDetails","waiting"));
//        mAppsList.add(new AppListData("Bromide", "An image sending app.", null));

    	super.onResume();
    	//sendInfoToHub();
    }

    public void addAppToList(String appName, String appDescription, String imageUri) {
        for (AppListData tempData : mAppsList) {
            if (tempData.appName.compareTo(appName) == 0) {
                return;
            }
        }
        mAppsList.add(new AppListData(appName, appDescription, imageUri));
        setupAppsList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.settings) {
			Intent selectedSettings = new Intent(this,SettingsActivity.class);
			startActivity(selectedSettings);
			return true;
		} else if (itemId == R.id.users) {
			Intent selectedUserList = new Intent(this,UserChooserActivity.class);
			startActivity(selectedUserList);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * @author aarthi
	 * Sending details DTN ID, MAC ADDRESS and CURRENT TIME TO SUTA Hub.
	 */
	public void sendInfoToHub()
	{
        SharedPreferences pref =PreferenceManager.getDefaultSharedPreferences(this);
        Action act = new Action("SendInfo", aeCtx);
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE); // arnavdhamija - Android N optimisation
        WifiInfo info = manager.getConnectionInfo();
        String macAddress = info.getMacAddress();
        act.addArgument("macAddress", macAddress);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String notifSentBySutaAt = sdf.format(c.getTime());
        act.addArgument("notifSentBySutaAt", notifSentBySutaAt);

        //adding time at hub  for last hub update sent by hub and received by suta
        String timeAtHubOfLastHubUpdate = pref.getString("notifSentByHubAt","-1");
        act.addArgument("timeAtHubOfLastHubUpdate",timeAtHubOfLastHubUpdate);

        //adding time at suta for last hub update sent by hub and received by suta
        String timeAtSutaOfLastHubUpdate = pref.getString("notifRecievedBySutaAt","-1");
        act.addArgument("timeAtSutaOfLastHubUpdate",timeAtSutaOfLastHubUpdate);


        String hubAddress = pref.getString("hub_address", "");
      
        if(null!=hubAddress) {
            Helper.sendSutaInfo(act, hubAddress + "/suta", this);
        }
	}

    @Override
    public void onStatusFragmentInteraction(String string) {

    }

    @Override
    public void onDownloadSelected(int position) {
        Toast.makeText(this, "Downloading " + mAppsList.get(position).appName, Toast.LENGTH_SHORT).show();
        String appRequested = mAppsList.get(position).appName;
        if (appRequested != null) {
            sendAppRequest(appRequested);
        }
    }
}
