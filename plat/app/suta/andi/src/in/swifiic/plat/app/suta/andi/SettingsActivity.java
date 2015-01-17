package in.swifiic.plat.app.suta.andi;

import in.swifiic.android.app.suta.R;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {

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
		bindPreferenceSummaryToValue(findPreference("hub_address"));
		bindPreferenceSummaryToValue(findPreference("my_identity"));
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();
			preference.setSummary(stringValue);
			Intent intent = new Intent();
			Log.d("Settings SUTA", "Preference key: " + preference.getKey() + " with value: " + stringValue);
			if(preference.getKey().equals("hub_address")) {
				intent.setAction("swifiic.suta.hubAddressUpdate");
				intent.putExtra("hubAddress", stringValue);
				Log.d("Settings SUTA", "Setting hub address in intent as: " + stringValue);
			} else if(preference.getKey().equals("my_identity")) {
				intent.setAction("swifiic.suta.myIdentityUpdate");
				intent.putExtra("myIdentity", stringValue);
				Log.d("Settings SUTA", "Setting identity in intent as: " + stringValue);
			}
			preference.getContext().sendBroadcast(intent);
			Log.d("SUTA", "Sent broadcast with intent: " + intent.toString() + " Extras: " + intent.getExtras());
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, 
				PreferenceManager
				.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
	}
}
