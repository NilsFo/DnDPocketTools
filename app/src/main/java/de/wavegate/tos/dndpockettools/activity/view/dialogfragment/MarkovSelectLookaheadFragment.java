package de.wavegate.tos.dndpockettools.activity.view.dialogfragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.ScrollView;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.activity.mainfragments.NameGeneratorFragment;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 26.06.2016.
 */

public class MarkovSelectLookaheadFragment extends DialogFragment {

	public static final int MAXIMUM_LOOKAHEAD = 10;

	private CheckBox checkBox;
	private NumberPicker numberPicker;

	private Listener listener;

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle(R.string.action_select_lookahead);

		if (savedInstanceState != null) {
			dismiss();
		}

		View view = inflater.inflate(R.layout.dialog_select_markov_lookahead, null);

		ScrollView scrollView = new ScrollView(getContext());
		scrollView.addView(view);
		builder.setView(scrollView);

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.triggered(getSelectedMode());
				}
			}
		});

		checkBox = (CheckBox) view.findViewById(R.id.markov_lookahead_cb);
		numberPicker = (NumberPicker) view.findViewById(R.id.markov_lookahead_spinner);

		numberPicker.setMinValue(1);
		numberPicker.setMaxValue(MAXIMUM_LOOKAHEAD);
		numberPicker.setWrapSelectorWheel(false);

		checkBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				numberPicker.setEnabled(!checkBox.isChecked());
			}
		});

		int mode = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(NameGeneratorFragment.PREFERENCES_CUSTOM_MARKOV_LOOKAHEAD, 0);
		Log.i(LOGTAG, "Markov Dialog created. Currently loaded mode: " + mode);
		if (mode == 0) {
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
			numberPicker.setValue(mode);
		}
		numberPicker.setEnabled(!checkBox.isChecked());

		return builder.create();
	}

	private int getSelectedMode() {
		Log.i(LOGTAG, "Evaluating dialog: CB selected: " + checkBox.isSelected() + " NumberPicker value: " + numberPicker.getValue());
		if (checkBox.isChecked()) return 0;
		return numberPicker.getValue();
	}

	public interface Listener {
		public void triggered(int mode);
	}
}
