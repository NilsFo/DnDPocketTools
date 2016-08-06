package de.wavegate.tos.dndpockettools.activity.mainfragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import de.wavegate.tos.dndpockettools.MainActivity;
import de.wavegate.tos.dndpockettools.R;

/**
 * Created by Nils on 01.04.2016.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.i(MainActivity.LOGTAG, "Preference changed: " + sharedPreferences + " key: " + key + " is now: " + getValue(sharedPreferences, key));
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	public String getValue(SharedPreferences sharedPreferences, String key) {
		if (!sharedPreferences.contains(key)) {
			return "<Unexisting Value>";
		}

		try {
			return String.valueOf(sharedPreferences.getBoolean(key, false));
		} catch (ClassCastException e) {
		}
		try {
			return String.valueOf(sharedPreferences.getFloat(key, 0f));
		} catch (ClassCastException e) {
		}
		try {
			return String.valueOf(sharedPreferences.getInt(key, 0));
		} catch (ClassCastException e) {
		}
		try {
			return String.valueOf(sharedPreferences.getLong(key, 0));
		} catch (ClassCastException e) {
		}
		try {
			return String.valueOf(sharedPreferences.getString(key, "<Missing String>"));
		} catch (ClassCastException e) {
		}

		return "<Unknwon Value>";
	}
}
