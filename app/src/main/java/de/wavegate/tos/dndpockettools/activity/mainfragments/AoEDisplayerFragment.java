package de.wavegate.tos.dndpockettools.activity.mainfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.data.aoe.Shape;
import de.wavegate.tos.dndpockettools.data.aoe.Shaper;


/**
 * Created by Nils on 07.07.2016.
 */

public class AoEDisplayerFragment extends MainMenuFragment {

	private Spinner sizeSP, shapeSP;
	private TableLayout table;

	private int size;
	private Shape shape;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.m_fragment_aoe_displayer, container, false);

		sizeSP = (Spinner) v.findViewById(R.id.aoe_size_spinner);
		shapeSP = (Spinner) v.findViewById(R.id.aoe_shape_spinner);

		table = (TableLayout) v.findViewById(R.id.aoe_table);

		sizeSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String s = getResources().getStringArray(R.array.values_aoe_size)[position];

				if (isNumeric(s)) {
					size = Integer.parseInt(s);
				} else {
					showCustomSizeDialog();
				}
				updateTable();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				updateTable();
			}
		});
		shapeSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Shape s = toShape(position);
				if (s == null) {
					return;
					//TODO display error, undefined shape;
				}
				shape = s;
				updateTable();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				updateTable();
			}
		});

		shape = Shape.LINE;
		size = 5;
		updateTable();

		informMainActivity(getContext());
		return v;
	}

	public void updateTable() {
		correctSize();

		Shaper shaper = new Shaper(size, shape);

		table.removeAllViews();
		table.setStretchAllColumns(true);

		int width = 15;
		int height = 15;
		shaper.offset(width / 2, 1);
		boolean[][] map = new boolean[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				map[i][j] = shaper.hasPoint(i, j);
			}
		}


		for (int h = 0; h < height; h++) {
			TableRow row = new TableRow(getContext());
			for (int w = 0; w < width; w++) {
				TextView text = new TextView(getContext());
				if (map[w][h]) text.setText("X");
				else text.setText("O");

				row.addView(text);
			}
			table.addView(row);
		}
	}

	private void showCustomSizeDialog() {

	}

	@Override
	public int getTitle() {
		return R.string.drawer_category_aoe_displayer;
	}

	private void correctSize() {
		while (size % 5 != 0) {
			size--;
		}

		if (size < 5) {
			sizeSP.setSelection(0);
		}
	}

	private boolean isNumeric(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private Shape toShape(int i) {
		switch (i) {
			case 0:
				return Shape.LINE;
			case 1:
				return Shape.CUBE;
			case 2:
				return Shape.CONE;
			case 3:
				return Shape.SPHERE;
		}
		return null;
	}
}