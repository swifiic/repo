package in.swifiic.plat.app.suta.andi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import android.view.Menu;
import android.view.MenuItem;
import in.swifiic.plat.helper.andi.xml.Action;
import in.swifiic.plat.helper.andi.AppEndpointContext;
import in.swifiic.plat.helper.andi.Constants;
import in.swifiic.plat.helper.andi.Helper;

public class MainActivity extends SwifiicActivity {

    @SuppressWarnings("unused")
	private final String TAG="MainActivity";
    private AppEndpointContext aeCtx = new AppEndpointContext("suta", "0.1", "1");
    public static SharedPreferences pref =null;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	
        super.onCreate(savedInstanceState);
        pref=PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main); 
        sendInfoToHub();

		Intent serviceIntent = new Intent();
		serviceIntent.setAction("in.swifiic.plat.app.suta.andi.mgmt.TrackService");
		startService(serviceIntent); 
		
    }
    public void onResume()
    {
    	super.onResume();
    	sendInfoToHub();
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
        String strDate = sdf.format(c.getTime());
        act.addArgument("dateTime",strDate);
         String hubAddress = pref.getString("hub_address", "");
      
        if(null!=hubAddress)

        Helper.sendSutaInfo(act, hubAddress + "/suta", this);
	}
	
}
