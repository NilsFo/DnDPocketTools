package de.wavegate.tos.dndpockettools.activity.view.dialogfragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.wavegate.tos.dndpockettools.R;

/**
 * Created by Nils on 07.04.2016.
 */
public class NewCampaignFragment extends DialogFragment {

	private NewCampaignFragment.DialogListener listener;
	private EditText nameTF, gmnameTF;

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
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle(R.string.action_new_campiagn);
		builder.setIcon(R.drawable.random);

		if (savedInstanceState != null) {
			dismiss();
		}

		View view = inflater.inflate(R.layout.dialog_new_campaign, null);
		builder.setView(view);

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
					listener.triggered(nameTF.getText().toString(), gmnameTF.getText().toString());
				}
			}
		});

		nameTF = (EditText) view.findViewById(R.id.new_campaign_name);
		gmnameTF = (EditText) view.findViewById(R.id.new_campaing_gmname);

		return builder.create();
	}

	public interface DialogListener {
		public void triggered(String name, String gmName);
	}
}
