package in.swifiic.plat.app.suta.andi;

import in.swifiic.plat.app.suta.andi.R;
import in.swifiic.plat.app.suta.andi.provider.Provider;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView.FindListener;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
	public static SharedPreferences sharedPref=null;
	public static String cachedHubAddres=null;
	public static String cachedIdentity=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Bind the summaries of EditText to their values.
		sharedPref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
		cachedHubAddres=sharedPref.getString("hub_address",cachedHubAddres);
		cachedIdentity=sharedPref.getString("my_identity",cachedIdentity);
	    
		// save the old value here in global class variable TODO XXX
		bindPreferenceSummaryToValue(findPreference("hub_address"));
		bindPreferenceSummaryToValue(findPreference("my_identity"));
		bindPreferenceSummaryToValue(findPreference("reset_required"));
	}

	private class PrefChangeLstnr implements Preference.OnPreferenceChangeListener {
		Preference preferenceRef = null;
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			final String stringValue = value.toString();
			preference.setSummary(stringValue);
			preferenceRef = preference;

			if(!(preference.getKey().equals("hub_address")) && !( preference.getKey().equals("my_identity"))){
				// no change of interest to us - just return;
				return false;
			}
			if(preference.getKey().equals("hub_address") && stringValue.equals(cachedHubAddres)){
				Intent intent = new Intent();
				intent.setAction("swifiic.suta.hubAddressUpdate");
				Log.d("Settings SUTA", "Preference key: " + PrefChangeLstnr.this.preferenceRef.getKey() + " with value: " + stringValue);
				intent.putExtra("hubAddress",sharedPref.getString("hub_address", cachedHubAddres));
				intent.putExtra("identity",sharedPref.getString("my_identity", cachedIdentity));
				intent.putExtra("resetrequired",sharedPref.getString("reset_required", "no"));
				SettingsActivity.this.sendBroadcast(intent);
				Log.d("SUTA", "Sent broadcast with intent: " + intent.toString() + " Extras: " + intent.getExtras());
				return false; // no change
			}
			if(preference.getKey().equals("my_identity") && stringValue.equals(cachedIdentity)){
				return false; // no change
			}
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(preference.getContext());

			alertDialog.setTitle("Alert");
			alertDialog.setMessage("Are you sure that you want to change the hub address");
			alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int which) {
					Provider.providerInstance.deletedB();	
					sharedPref.edit().putString("reset_required", "yes").commit();
					if(PrefChangeLstnr.this.preferenceRef.getKey().equals("hub_address"))
					sharedPref.edit().putString("hub_address", stringValue).commit();
					if(PrefChangeLstnr.this.preferenceRef.getKey().equals("my_identity"))
					sharedPref.edit().putString("my_identity", stringValue).commit();
					Intent intent = new Intent();
					intent.setAction("swifiic.suta.hubAddressUpdate");
					Log.d("Settings SUTA", "Preference key: " + PrefChangeLstnr.this.preferenceRef.getKey() + " with value: " + stringValue);
					intent.putExtra("hubAddress",sharedPref.getString("hub_address", cachedHubAddres));
					intent.putExtra("identity",sharedPref.getString("my_identity", cachedIdentity));
					intent.putExtra("resetrequired",sharedPref.getString("reset_required", "no"));
					SettingsActivity.this.sendBroadcast(intent);
					Log.d("SUTA", "Sent broadcast with intent: " + intent.toString() + " Extras: " + intent.getExtras());
					

				}
			});
			alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					sharedPref.edit().putString("reset_required", "no").commit();
					sharedPref.edit().putString("hub_address", cachedHubAddres).commit();
					sharedPref.edit().putString("my_identity",cachedIdentity).commit();
					dialog.cancel();
				}
			});
			// Showing Alert Message
			alertDialog.show();
			return false;
		}
	}
	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static PrefChangeLstnr prefLstnr = null;

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private void bindPreferenceSummaryToValue(Preference preference) {
		
		if(null == prefLstnr)
			prefLstnr = new PrefChangeLstnr();
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(prefLstnr);

		// Trigger the listener immediately with the preference's
		// current value.
		prefLstnr.onPreferenceChange(preference, 
				PreferenceManager
				.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
	}
}
