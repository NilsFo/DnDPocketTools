package de.wavegate.tos.dndpockettools.activity.view.dialogfragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.data.PlayerCharacter;
import de.wavegate.tos.dndpockettools.drawable.AlignmentBackground;
import de.wavegate.tos.dndpockettools.util.Alignments;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 08.04.2016.
 */
public class ShowPartyAlignmentFragment extends DialogFragment {

	private ArrayList<PlayerCharacter> party;
	private TextView lg;
	private TextView ng;
	private TextView cg;
	private TextView ln;
	private TextView n;
	private TextView cn;
	private TextView le;
	private TextView ne;
	private TextView ce;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle(R.string.actions_show_party_alignment);
		builder.setIcon(R.drawable.ic_action_group);
		if (savedInstanceState != null) {
			dismiss();
		}
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		View view = inflater.inflate(R.layout.dialog_party_alignment, null);
		builder.setView(view);

		lg = (TextView) view.findViewById(R.id.dialog_party_alignment_lg);
		ng = (TextView) view.findViewById(R.id.dialog_party_alignment_ng);
		cg = (TextView) view.findViewById(R.id.dialog_party_alignment_cg);
		ln = (TextView) view.findViewById(R.id.dialog_party_alignment_ln);
		n = (TextView) view.findViewById(R.id.dialog_party_alignment_n);
		cn = (TextView) view.findViewById(R.id.dialog_party_alignment_cn);
		le = (TextView) view.findViewById(R.id.dialog_party_alignment_le);
		ne = (TextView) view.findViewById(R.id.dialog_party_alignment_ne);
		ce = (TextView) view.findViewById(R.id.dialog_party_alignment_ce);
		HashMap<Integer, Integer> alignmentMap = new HashMap<>();

		int highestCount = 0;
		for (PlayerCharacter character : party) {
			int alignment = character.getAlignment();
			Integer alignmentCounter = alignmentMap.get(alignment);
			if (alignmentCounter == null) {
				alignmentCounter = 0;
			}
			if (alignment != 0) highestCount++;
			alignmentCounter++;
			//if (alignmentCounter > highestCount)highestCount = alignmentCounter;
			alignmentMap.put(alignment, alignmentCounter);
		}
		Log.i(LOGTAG, "Analyzed party alignments. Individual alignments: " + alignmentMap.keySet().size() + ", highestCount: " + highestCount);

		for (int i = 1; i < 10; i++) {
			TextView v = getAlignmentView(i);
			if (v != null) {
				AlignmentBackground background;
				if (alignmentMap.containsKey(i)) {
					double percentage = ((double) alignmentMap.get(i) / (double) highestCount) / 2;
					percentage = 0.5 - percentage;
					background = new AlignmentBackground(percentage);
				} else {
					background = new AlignmentBackground(1f);
				}
				v.setBackground(background);
			} else {
				Log.w(LOGTAG, "Wanting to find a TextView for alignment " + i + " but none was found! In words: " + Alignments.get(getContext()).getLong(i));
			}
		}
		return builder.create();
	}

	public void setParty(ArrayList<PlayerCharacter> party) {
		this.party = party;
	}

	private TextView getAlignmentView(int alignment) {
		switch (alignment) {
			case 1:
				return lg;
			case 2:
				return ng;
			case 3:
				return cg;
			case 4:
				return ln;
			case 5:
				return n;
			case 6:
				return cn;
			case 7:
				return le;
			case 8:
				return ne;
			case 9:
				return ce;
			case 10:
				return lg;
			default:
				return null;
		}
	}

}

