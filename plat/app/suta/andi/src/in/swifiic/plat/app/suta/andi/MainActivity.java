package in.swifiic.plat.app.suta.andi;

import in.swifiic.plat.app.suta.andi.R;
import in.swifiic.plat.helper.andi.ui.SwifiicActivity;
import in.swifiic.plat.helper.andi.ui.UserChooserActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends SwifiicActivity {

    @SuppressWarnings("unused")
	private final String TAG="MainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("in.swifiic.plat.app.suta.andi.mgmt.TrackService");
		startService(serviceIntent);       
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.settings) {
			Intent selectedSettings = new Intent(this, SettingsActivity.class);
			startActivity(selectedSettings);
			return true;
		} else if (itemId == R.id.users) {
			Intent selectedUserList = new Intent(this, UserChooserActivity.class);
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
}
