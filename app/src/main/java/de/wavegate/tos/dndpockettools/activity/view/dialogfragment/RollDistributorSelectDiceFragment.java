package de.wavegate.tos.dndpockettools.activity.view.dialogfragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.util.DiceCalculator;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 04.04.2016.
 */
public class RollDistributorSelectDiceFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

	public static final String STORED_N = "stored_n";
	public static final String STORED_R = "stored_r";

	public static final int[] DEFAULT_DICE_VALUES = {2, 3, 4, 6, 8, 10, 12, 20, 100};

	private TextView previewLB;
	private NumberPicker picker_n, picker_r;
	private DialogListener listener;
	private DiceCalculator oldCalculator;
	private Spinner spinner;

	public DialogListener getListener() {
		return listener;
	}

	public void setListener(DialogListener listener) {
		this.listener = listener;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.roll_chooser_dialog, container, false);

		//	previewLB = (TextView) view.findViewById(R.id.roll_chooser_preview);
		//	picker_n = (NumberPicker) view.findViewById(R.id.roll_chooser_amount);
		//	picker_r = (NumberPicker) view.findViewById(R.id.roll_chooser_value);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle(R.string.action_edit_dice);
		builder.setIcon(R.drawable.random);

		if (savedInstanceState != null) {
			dismiss();
		}

		View view = inflater.inflate(R.layout.roll_chooser_dialog, null);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(LOGTAG, "There will be sb triggered... " + picker_n.getValue() + " " + picker_r.getValue());
				if (listener != null) {

					listener.triggered(picker_n.getValue(), picker_r.getValue());
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		previewLB = (TextView) view.findViewById(R.id.roll_chooser_preview);
		picker_n = (NumberPicker) view.findViewById(R.id.roll_chooser_amount);
		picker_r = (NumberPicker) view.findViewById(R.id.roll_chooser_value);
		spinner = (Spinner) view.findViewById(R.id.roll_chooser_spinner);

		picker_n.setMinValue(1);
		picker_n.setMaxValue(9999);
		picker_n.setWrapSelectorWheel(true);
		picker_r.setMinValue(1);
		picker_r.setMaxValue(9999);
		picker_r.setValue(2);
		picker_r.setWrapSelectorWheel(true);
		picker_n.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				updateTextField();
			}
		});
		picker_r.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				//	boolean found = false;
				//	for (int i = 0; i < DEFAULT_DICE_VALUES.length; i++) {
				//		int value = DEFAULT_DICE_VALUES[i];
				//		if (value == newVal) {
				//			spinner.setSelection(i + 1);
				//			found = true;
				//		}
				//	}
				//	if (!found)
				if (spinner.getSelectedItemPosition() != 0)
					spinner.setSelection(0);

				updateTextField();
			}
		});

		ArrayList<String> spinnerList = new ArrayList<>();
		spinnerList.add(getString(R.string.custom));
		for (int dice : DEFAULT_DICE_VALUES) {
			String s = getString(R.string.dice_shortcut) + dice;
			spinnerList.add(s);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerList);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		if (getOldCalculator() != null) {
			picker_n.setValue(getOldCalculator().getDiceCount());
			picker_r.setValue(getOldCalculator().getDiceValue());
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STORED_N) && savedInstanceState.containsKey(STORED_R)) {
			picker_n.setValue(savedInstanceState.getInt(STORED_N));
			picker_r.setValue(savedInstanceState.getInt(STORED_R));
		}

		updateTextField();
		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(STORED_N, picker_n.getValue());
		outState.putInt(STORED_R, picker_r.getValue());
	}

	public DiceCalculator getOldCalculator() {
		return oldCalculator;
	}

	public void setOldCalculator(DiceCalculator oldCalculator) {
		this.oldCalculator = oldCalculator;
	}

	public String getDiceCode() {
		return picker_n.getValue() + getString(R.string.dice_shortcut) + picker_r.getValue();
	}

	public void updateTextField() {
		previewLB.setText(String.format(getString(R.string.preview), getDiceCode()));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Log.i(LOGTAG, "There has been a selection: Pos. " + position + ": " + parent.getItemAtPosition(position));
		if (position != 0)
			picker_r.setValue(DEFAULT_DICE_VALUES[position - 1]);
		updateTextField();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	public interface DialogListener {
		public void triggered(int n, int r);
	}
}
