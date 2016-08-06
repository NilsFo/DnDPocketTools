package de.wavegate.tos.dndpockettools.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.NumberPicker;

/**
 * Created by Nils on 03.04.2016.
 */
public final class DialogUtils {

	public static void textInput(Context context, int title, EditText input, DialogInterface.OnClickListener onClickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getString(title));
		builder.setView(input);

		builder.setPositiveButton(android.R.string.ok, onClickListener);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	public static void selectNumber(Context context, int title, int minValue, int maxValue, int defaultValue, NumberPickerListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getString(title));

		final NumberPicker np = new NumberPicker(context);
		np.setOnValueChangedListener(listener);
		np.setMaxValue(maxValue);
		np.setMinValue(minValue);
		np.setValue(defaultValue);
		np.setWrapSelectorWheel(false);
		listener.setValue(defaultValue);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setPositiveButton(android.R.string.ok, listener);
		builder.setView(np);

		builder.show();
	}

	public static abstract class NumberPickerListener implements NumberPicker.OnValueChangeListener, DialogInterface.OnClickListener {

		private int value;

		public void setValue(int value) {
			this.value = value;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			triggered(dialog, which, value);
		}

		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
			value = newVal;
		}

		public abstract void triggered(DialogInterface dialog, int which, int selectedValue);
	}

}
