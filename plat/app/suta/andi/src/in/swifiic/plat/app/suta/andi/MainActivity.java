package in.swifiic.plat.app.suta.andi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import in.swifiic.plat.app.suta.andi.R;
import in.swifiic.plat.helper.andi.ui.SwifiicActivity;
import in.swifiic.plat.helper.andi.ui.UserChooserActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import in.swifiic.plat.helper.andi.xml.Action;
import in.swifiic.plat.helper.andi.AppEndpointContext;
import in.swifiic.plat.helper.andi.Constants;
import in.swifiic.plat.helper.andi.Helper;

public class MainActivity extends SwifiicActivity  {


    @SuppressWarnings("unused")
    private final long timeDiff = 1*(60*1000); // in milli seconds
	private final String TAG="MainActivity";
    private AppEndpointContext aeCtx = new AppEndpointContext("suta", "0.1", "1");
    public static SharedPreferences pref =null;

    private TextView remainingCredit,currTime,transactions;

   
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        pref=PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);


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
        if (diff>timeDiff || (diff == 0)){
            sendInfoToHub();
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("lastUpdatedTime",scurTime);
            editor.commit();

        }

        //

        remainingCredit = (TextView)findViewById(R.id.remainingCredit);
        currTime = (TextView)findViewById(R.id.currTime);
        transactions = (TextView)findViewById(R.id.transactions);
        transactions.setMovementMethod(new ScrollingMovementMethod());

        remainingCredit.setText(pref.getString("remainingCredit", "waiting"));
        currTime.setText(pref.getString("notifSentByHubAt","waiting"));
        transactions.setText(pref.getString("revisedTransactionDetails","waiting"));

		Intent serviceIntent = new Intent();
		serviceIntent.setAction("in.swifiic.plat.app.suta.andi.mgmt.TrackService");
		startService(serviceIntent); 
		
    }
    public void onResume()
    {
        remainingCredit.setText(pref.getString("remainingCredit", "waiting"));
        currTime.setText(pref.getString("notifSentByHubAt","waiting"));
        transactions.setText(pref.getString("revisedTransactionDetails","waiting"));

    	super.onResume();
    	//sendInfoToHub();
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
		Action act = new Action("SendInfo", aeCtx);
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
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
      
        if(null!=hubAddress)
        Helper.sendSutaInfo(act, hubAddress + "/suta", this);
	}

}
