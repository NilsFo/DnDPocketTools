package de.wavegate.tos.dndpockettools.activity.mainfragments;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.activity.view.dialogfragment.RollDistributorSelectDiceFragment;
import de.wavegate.tos.dndpockettools.util.DialogUtils;
import de.wavegate.tos.dndpockettools.util.DiceCalculator;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Uses charting: https://github.com/PhilJay/MPAndroidChart
 */

public class RollDistributionsFragment extends MainMenuFragment implements OnChartValueSelectedListener {

	public static final String BUNDLE_N = "bundle_n";
	public static final String BUNDLE_R = "bundle_r";
	public static final String PREFERENCE_IGNORE_ROLL_MESSAGE = "prefs_ignore_roll_message";
	public static final int MAX_DECIMAL_CUTOFFS = 13;
	public static final int MIN_DECIMAL_CUTOFFS = 2;
	private DiceCalculator diceCalculator;
	private LineChart chart;
	private Toast rollResultToast, chartSelectionToast;
	private boolean showAnimation = true;

	public RollDistributionsFragment() {
		super();
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.i(LOGTAG, "RollDistribution says: I am onCreate() My Context: " + getContext().getClass().getCanonicalName());
		View root = inflater.inflate(R.layout.m_fragment_roll_distributions, container, false);

		informMainActivity(getContext());

		showAnimation = true;
		chart = (LineChart) root.findViewById(R.id.chart);
		XAxis xAxis = chart.getXAxis();

		chart.setOnChartValueSelectedListener(this);
		chart.setTouchEnabled(true);
		chart.getLegend().setEnabled(false);
		xAxis.setAvoidFirstLastClipping(false);

		//	if (getContext() instanceof MainActivity) {
		//		MainActivity mc = (MainActivity) getContext();
		//		mc.onFragmentChange(this);
		//	}

		if (savedInstanceState != null) {
			int n = savedInstanceState.getInt(BUNDLE_N, -1);
			int r = savedInstanceState.getInt(BUNDLE_R, -1);
			Log.i(LOGTAG, "Inside this fragment remembers something... n:" + n + " r:" + r);
			if (n != -1 && r != -1) {
				showAnimation = false;
				setDiceCalculator(new DiceCalculator(n, r));
			}
		}
		//setDiceCalculator(new DiceCalculator(4, 8));

		return root;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(LOGTAG, "I got some options parsed: " + item);

		switch (item.getItemId()) {
			case R.id.action_editDice:
				showEditDiceMenu();
				return true;
			case R.id.action_roll_something:
				rollSomething();
				return true;
			case R.id.action_change_input_via_text:
				showEditDiceInputField();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showEditDiceMenu() {
		RollDistributorSelectDiceFragment fragment = new RollDistributorSelectDiceFragment();
		fragment.setListener(new RollDistributorSelectDiceFragment.DialogListener() {
			@Override
			public void triggered(int n, int r) {
				Log.i(LOGTAG, "I am triggered! n:" + n + " r:" + r);
				setDiceCalculator(new DiceCalculator(n, r));
			}
		});
		if (diceCalculator != null) {
			fragment.setOldCalculator(diceCalculator);
		}

		fragment.show(getActivity().getSupportFragmentManager(), "new_edit_dice_dialog");
	}

	private void showEditDiceInputField() {
		final EditText text = new EditText(getContext());
		text.setMaxLines(1);
		if (diceCalculator != null) {
			text.setText(getDiceString());
		}
		text.setHint(R.string.dice_calculator_roll_hint);
		text.setInputType(InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		DialogUtils.textInput(getContext(), R.string.action_edit_dice, text, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String s = text.getText().toString();
				Log.i(LOGTAG, "Recieved text: " + s);
				showAnimation = true;
				setDiceCalculator(s);
			}
		});
	}

	private void rollSomething() {
		if (diceCalculator == null) return;

		int i = diceCalculator.getSample();
		double p = diceCalculator.getProbability(i) * 100;
		String s = String.format(getString(R.string.dice_calculator_roll_result), getDiceString(), String.valueOf(i), formatDecimal(p) + "%");
		if (rollResultToast == null)
			rollResultToast = Toast.makeText(getContext(), "", Toast.LENGTH_LONG);
		rollResultToast.setText(s);
		rollResultToast.show();
		chart.highlightValue(i - diceCalculator.getMinValue(), 0);
	}

	public void setDiceCalculator(String source) {
		String regex = "(\\d+)(\\D+)(\\d+)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(source);
		if (m.matches()) {
			Log.i(LOGTAG, "L: " + m.group(1) + " M: " + m.group(2) + " R: " + m.group(3));
			int n = Integer.parseInt(m.group(1));
			int r = Integer.parseInt(m.group(3));
			setDiceCalculator(new DiceCalculator(n, r));

			if (!m.group(2).toLowerCase().equals(getString(R.string.dice_shortcut).toLowerCase()) || !source.equals(m.group(1) + m.group(2) + m.group(3))) {
				String dice = n + getString(R.string.dice_shortcut) + r;
				String s = String.format(getString(R.string.dice_interpreted), dice);
				Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(getContext(), R.string.error_invalid_input, Toast.LENGTH_LONG).show();
		}
	}

	public void setDiceCalculator(DiceCalculator diceCalculator) {
		this.diceCalculator = diceCalculator;
		try {
			diceCalculator.calculateAllProbabilities();
		} catch (Exception e) {
			String s = "An " + e.getClass().getName() + "-Exception occurred: '" + e.getMessage() + "'";
			Log.e(LOGTAG, s, e);
			//Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
			diceCalculator.rollAllProbabilities();
			e.printStackTrace();
			informAboutCalculationException();
		}
		ArrayList<Entry> data = new ArrayList<>();
		ArrayList<String> descriptions = new ArrayList<>();
		int counter = 0;
		for (int i = diceCalculator.getMinValue(); i < diceCalculator.getMaxValue() + 1; i++) {
			float f = (float) (diceCalculator.getProbability(i) * 100);
			data.add(new Entry((float) f, counter));
			descriptions.add(String.valueOf(i));
			counter++;
		}
		LineDataSet set = new LineDataSet(data, "Rolls");
		set.setAxisDependency(YAxis.AxisDependency.LEFT);

		LineData lineData = new LineData(descriptions, set);
		lineData.setValueFormatter(new PercentFormatter());
		chart.setData(lineData);
		chart.setDescription(getDiceString());
		chart.invalidate();

		if (showAnimation) {
			chart.animateX(1337, Easing.EasingOption.EaseInCubic);
		}
	}

	private void informAboutCalculationException() {
		if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(PREFERENCE_IGNORE_ROLL_MESSAGE, false))
			Toast.makeText(getContext(), R.string.error_dice_calculation_falied_short, Toast.LENGTH_LONG).show();
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(R.string.error_dice_calculation_falied_short).setIcon(android.R.drawable.ic_dialog_alert);
			builder.setMessage(R.string.error_dice_calculation_falied);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(R.string.ok_dont_show_again, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(PREFERENCE_IGNORE_ROLL_MESSAGE, true).commit();
				}
			});
			builder.show();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (diceCalculator == null) {
			return;
		}
		outState.putInt(BUNDLE_N, diceCalculator.getDiceCount());
		outState.putInt(BUNDLE_R, diceCalculator.getDiceValue());
	}

	@Override
	public int getTitle() {
		return R.string.drawer_category_roll_distributor;
	}

	@Override
	public int getMenu() {
		return R.menu.roll_distributor_fragment;
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(this, uri);
		}
	}

	public String getDiceString() {
		if (diceCalculator == null) return getString(R.string.error_unknown);
		return diceCalculator.getDiceCount() + getString(R.string.dice_shortcut) + diceCalculator.getDiceValue();
	}

	@Override
	public void onValueSelected(Entry entry, int i, Highlight highlight) {
		Log.i(LOGTAG, "A chart value was selected: " + entry);
		if (chartSelectionToast == null) {
			chartSelectionToast = Toast.makeText(getContext(), "", Toast.LENGTH_LONG);
		}

		//Log.i(LOGTAG, "test " + diceCalculator.getProbability(entry.getXIndex() + diceCalculator.getMinValue()));
		int roll = entry.getXIndex() + diceCalculator.getMinValue();
		double p = diceCalculator.getProbability(roll) * 100;
		chartSelectionToast.setText(String.format(getString(R.string.dice_calculator_roll_selected), String.valueOf(roll), formatDecimal(p) + "%"));
		chartSelectionToast.show();
	}

	public String formatDecimal(double value, int digits) {
		String pattern = "0.";
		for (int i = 0; i < digits; i++) {
			pattern = pattern + "0";
		}
		DecimalFormat format = new DecimalFormat(pattern);
		format.setRoundingMode(RoundingMode.DOWN);

		return format.format(value);
	}

	public String formatDecimal(double value) {
		String defaultFormatted = formatDecimal(value, MIN_DECIMAL_CUTOFFS);
		if (defaultFormatted.charAt(0) != '0')
			return defaultFormatted;

		for (int i = MIN_DECIMAL_CUTOFFS; i < MAX_DECIMAL_CUTOFFS; i++) {
			String formatted = formatDecimal(value, i);
			String formattedDecimals = formatted.substring(2);
			if (TextUtils.isDigitsOnly(formattedDecimals)) {
				int decimals = Integer.parseInt(formattedDecimals);
				if (!(decimals == 0)) {
					return formatted;
				}
			}
		}
		return defaultFormatted;
	}

	@Override
	public void onNothingSelected() {

	}
}
