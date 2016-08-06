package de.wavegate.tos.dndpockettools.activity.view.dialogfragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.data.PlayerCharacter;
import de.wavegate.tos.dndpockettools.util.Alignments;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 08.04.2016.
 */
public class NewPlayerCharacterFragment extends DialogFragment {

	private DialogListener listener;
	private PlayerCharacter myCharacter;
	private EditText nameTF, playernameTF, xpTF;
	private Spinner alignmentSP;
	private TextView levelLB;

	public DialogListener getListener() {
		return listener;
	}

	public void setListener(DialogListener listener) {
		this.listener = listener;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle(R.string.action_add_new_character);
		builder.setIcon(R.drawable.ic_action_add_person_black);
		if (savedInstanceState != null) {
			dismiss();
		}

		View view = inflater.inflate(R.layout.dialog_new_playercharacter, null);
		//View view = inflater.inflate(R.layout.debug_test_layout, null);
		builder.setView(view);
		nameTF = (EditText) view.findViewById(R.id.create_character_name);
		playernameTF = (EditText) view.findViewById(R.id.create_character_playername);
		xpTF = (EditText) view.findViewById(R.id.create_character_xp);
		alignmentSP = (Spinner) view.findViewById(R.id.create_character_alignment);
		levelLB = (TextView) view.findViewById(R.id.create_character_level);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Alignments.get(getContext()).getLong());
		alignmentSP.setAdapter(adapter);

		xpTF.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateLevelTF();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

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
					listener.triggered(apply());
				}
			}
		});

		if (myCharacter != null) {
			builder.setTitle(R.string.action_edit_character);
			nameTF.setText(myCharacter.getName());
			playernameTF.setText(myCharacter.getPlayerName());
			xpTF.setText(String.valueOf(myCharacter.getXP()));
			alignmentSP.setSelection(myCharacter.getAlignment());
		}

		updateLevelTF();
		return builder.create();
	}

	private PlayerCharacter apply() {
		String name = nameTF.getText().toString();
		String playerName = playernameTF.getText().toString();
		int XP = 0;
		try {
			XP = Integer.parseInt(xpTF.getText().toString());
		} catch (NumberFormatException e) {
			XP = 0;
			Log.w(LOGTAG, "Unable to read '" + xpTF.getText() + "' and transform it into XP! Changed to 0");
		}

		int alignment = alignmentSP.getSelectedItemPosition();
		if (myCharacter == null) {
			return new PlayerCharacter(name, 0, 0, XP, playerName, alignment);
		}
		myCharacter.setName(name);
		myCharacter.setPlayerName(playerName);
		myCharacter.setXP(XP);
		myCharacter.setAlignment(alignment);
		return myCharacter;
	}

	private void updateLevelTF() {
		String lvl;
		try {
			int XP = Integer.parseInt(xpTF.getText().toString());
			int level = PlayerCharacter.getLevel(XP);
			lvl = String.valueOf(level);
		} catch (NumberFormatException e) {
			Log.w(LOGTAG, "Invalid number input!");
			lvl = "?";
		}

		levelLB.setText(String.format(getContext().getString(R.string.dialog_new_player_level), lvl));
	}

	public void setCharacterToEdit(PlayerCharacter myCharacter) {
		this.myCharacter = myCharacter;
	}

	public interface DialogListener {
		public void triggered(PlayerCharacter character);
	}
}

