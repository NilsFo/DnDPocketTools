package de.wavegate.tos.dndpockettools.activity.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.codecrafters.tableview.TableDataAdapter;
import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.data.PlayerCharacter;
import de.wavegate.tos.dndpockettools.util.Alignments;

/**
 * Created by Nils on 07.04.2016.
 */
public class PlayerCharacterDataAdapter extends TableDataAdapter<PlayerCharacter> {

	public PlayerCharacterDataAdapter(Context context, List<PlayerCharacter> data) {
		super(context, data);
	}

	@Override
	public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
		TextView view = new TextView(getContext());
		view.setText(R.string.error_unknown);
		view.setTextSize(14);
		//view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
		PlayerCharacter character = getRowData(rowIndex);

		switch (columnIndex) {
			case 0:
				view.setText(character.getName());
				break;
			case 1:
				view.setText(character.getPlayerName());
				break;
			case 2:
				view.setText(String.valueOf(character.getXP()));
				break;
			case 3:
				view.setText(String.valueOf(character.getLevel()));
				break;
			case 4:
				view.setText(Alignments.get(getContext()).getShort(character.getAlignment()));
				break;
		}

		view.setPadding(2, 2, 2, 2);
		return view;
	}
}
