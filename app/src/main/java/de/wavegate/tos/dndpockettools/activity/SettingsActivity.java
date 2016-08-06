package de.wavegate.tos.dndpockettools.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import de.wavegate.tos.dndpockettools.activity.mainfragments.SettingsFragment;

/**
 * Created by Nils on 01.04.2016.
 */
public class SettingsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
	}

}
