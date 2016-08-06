package de.wavegate.tos.dndpockettools.activity.view.dialogfragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.data.Campaign;

/**
 * Created by Nils on 11.06.2016.
 */
public class QuickSelectMarkovDialogFragment extends DialogFragment{

	private DialogListener listener;
	private ListView listView;

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
		builder.setTitle(R.string.action_quick_select);
		if (savedInstanceState != null) {
			dismiss();
		}

		listView = new ListView(getContext());

		ArrayList<Campaign> campaigns = Campaign.loadAll(getContext());
		Campaign[] data = new Campaign[campaigns.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = campaigns.get(i);
		}

		SelectCampaignRowAdapter adapter = new SelectCampaignRowAdapter(getContext(), data);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Campaign campaign = (Campaign) listView.getItemAtPosition(position);
				if (campaign != null && getListener() != null) {
					dismiss();
					listener.triggered(campaign);
				}
			}
		});

		builder.setView(listView);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		return builder.create();
	}

	public interface DialogListener {
		public void triggered(Campaign campaign);
	}

	public class SelectCampaignRowAdapter extends ArrayAdapter<Campaign> {
		public SelectCampaignRowAdapter(Context context, Campaign[] resource) {
			super(context, R.layout.dialog_select_campaign_fragment_row, resource);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			View view = inflater.inflate(R.layout.dialog_select_campaign_fragment_row, parent, false);

			Campaign campaign = getItem(position);
			TextView gm_nameTF = (TextView) view.findViewById(R.id.dialog_select_campaign_fragment_gm);
			TextView playersTF = (TextView) view.findViewById(R.id.dialog_select_campaign_fragment_player_count);
			TextView nameTF = (TextView) view.findViewById(R.id.dialog_select_campaign_fragment_campaign_name);

			nameTF.setText(campaign.getName());
			gm_nameTF.setText(String.format(getContext().getString(R.string.dialog_campaign_gmname_dynamic), campaign.getGMName()));
			playersTF.setText(String.format(getContext().getString(R.string.dialog_campaign_player_count), String.valueOf(campaign.countPlayerCharacters(getContext()))));

			return view;
		}
	}

}
