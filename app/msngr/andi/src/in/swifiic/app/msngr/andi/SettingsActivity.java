package in.swifiic.app.msngr.andi;

import in.swifiic.app.msngr.andi.R;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

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
	private static Preference.OnPreferenceChangeListener prefChangeListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			if(preference.getKey().equals("hub_address")) {
				String stringValue = value.toString();
				preference.setSummary(stringValue + " - Set from SUTA");
				return true;
			}
			else if(preference.getKey().equals("my_identity")) {
				String stringValue = value.toString();
				preference.setSummary(stringValue + " - Set from SUTA");
				return true;
			}
			return false;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #prefChangeListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(prefChangeListener);

		// Trigger the listener immediately with the preference's
		// current value.
		prefChangeListener.onPreferenceChange(preference, 
				PreferenceManager.getDefaultSharedPreferences
				(preference.getContext())
				.getString(preference.getKey(), ""));
	}
}
