package de.wavegate.tos.dndpockettools.activity.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import de.wavegate.tos.dndpockettools.MainActivity;
import de.wavegate.tos.dndpockettools.R;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 23.05.2016.
 */
public class NameGeneratorListFragment extends Fragment {

	public static final String ARGUMENT_FILENAME = "argument_filename";
	public static final String ARGUMENT_IS_INFO_FRAGMENT = "argument_is_info_fragment";
	public static final int MARKOV_RESULT_COLUMN_MIN_WIDTH = 240;
	public static final int INFO_TEXT_MARGIN = 16;

	private static final int PADDING = 16;
	private TableLayout table;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ScrollView scrollView = new ScrollView(getContext());
		scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		if (getArguments().getBoolean(ARGUMENT_IS_INFO_FRAGMENT, true)) {
			LinearLayout linearLayout = new LinearLayout(getContext());
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

			TextView textView = new TextView(getContext());
			textView.setText(R.string.more_categories_details);
			textView.setGravity(Gravity.CENTER);
			textView.setPadding(INFO_TEXT_MARGIN, INFO_TEXT_MARGIN * 2, INFO_TEXT_MARGIN, INFO_TEXT_MARGIN);

			ImageView imageView = new ImageView(getContext());
			imageView.setImageResource(R.drawable.hmm_info_edited_small_arrow);
			imageView.setAdjustViewBounds(true);
			imageView.setPadding(INFO_TEXT_MARGIN, INFO_TEXT_MARGIN * 2, INFO_TEXT_MARGIN, INFO_TEXT_MARGIN);

			linearLayout.addView(textView);
			linearLayout.addView(imageView);
			linearLayout.setGravity(Gravity.CENTER);
			scrollView.addView(linearLayout);
		} else {
			table = new TableLayout(getContext());
			table.setPadding(PADDING, PADDING, PADDING, PADDING);
			table.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			table.setStretchAllColumns(true);

			String cacheFile = getArguments().getString(ARGUMENT_FILENAME);
			ArrayList<String> data = new ArrayList<>();
			if (cacheFile != null) {
				File file = new File(cacheFile);
				try {
					data = readFile(file);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(LOGTAG, "Error while loading the cached data from " + file.getAbsolutePath());
				}
			}

			setText(data);

			scrollView.addView(table);
		}
		return scrollView;
	}

	public void setText(Collection<String> text) {
		if (table == null) {
			Log.i(MainActivity.LOGTAG, "NameGeneratorListFragment: Wanted to update the text on NameGeneratorListFragment. My table is null. Nothing done.");
			return;
		}

		table.removeAllViews();
		int position = 0;
		int maxRowCount = getMaxMarkovRowCount();
		TableRow currentRow = new TableRow(getContext());

		while (text.size() % maxRowCount != 0)
			text.add("");

		for (String s : text) {
			if (position == 0) currentRow = new TableRow(getContext());

			TextView textView = new TextView(getContext());
			textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
			textView.setText(format(s));
			currentRow.addView(textView);

			position++;
			if (position == maxRowCount) {
				position = 0;
				table.addView(currentRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
			}
		}
	}

	private int getMaxMarkovRowCount() {
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;

		return width / MARKOV_RESULT_COLUMN_MIN_WIDTH;
	}

	private String format(String string) {
		string = string.trim().toLowerCase();
		if (string.length() <= 1) return string;

		String first = string.substring(0, 1).toUpperCase();
		return first + string.substring(1);
	}

	private ArrayList<String> readFile(File file) throws IOException {
		ArrayList<String> list = new ArrayList<>();
		FileInputStream stream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String strLine;

		while ((strLine = br.readLine()) != null) {
			list.add(strLine);
		}

		br.close();
		return list;
	}
}
