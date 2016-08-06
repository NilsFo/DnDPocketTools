package de.wavegate.tos.dndpockettools.activity.view.dialogfragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.util.FileManager;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 11.05.2016.
 */
public class SelectBackupFileFragment extends DialogFragment {

	private DialogListener listener;
	private ListView listView;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.actions_import_backup_instructions);
		if (savedInstanceState != null) {
			dismiss();
		}

		final FileManager manager = new FileManager(getContext());
		ArrayList<File> fileList = new ArrayList<>();
		for (File file : manager.getCampaignBackupDir().listFiles()) {
			String name = file.getName();
			if (name.endsWith(".xml"))
				fileList.add(file);
		}
		Collections.sort(fileList);

		if (fileList.size() > 0) {
			File[] files = new File[fileList.size()];
			for (int i = 0; i < files.length; i++) {
				files[i] = fileList.get(i);
			}

			SelectBackupRowAdapter adapter = new SelectBackupRowAdapter(getContext(), files);
			listView = new ListView(getContext());
			listView.setAdapter(adapter);
			builder.setView(listView);
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String name = parent.getItemAtPosition(position).toString();
					if (getListener() != null) {
						dismiss();
						//listener.triggered(new File(manager.getCampaignBackupDir(), name + ".xml"));
						listener.triggered((File) parent.getItemAtPosition(position));
					}
				}
			});
		} else {
			builder.setMessage(R.string.error_no_backup_files_found);
		}

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		return builder.create();
	}

	//public int getValidFileCount() {
	//	if (fileList == null) {
	//		Log.e(LOGTAG, "FILE LIST IS NULL");
	//	}
	//	return fileList.size();
	//}
//
//	public boolean hasValidFileCount() {
//		return getValidFileCount() > 0;
//	}

	public DialogListener getListener() {
		return listener;
	}

	public void setListener(DialogListener listener) {
		this.listener = listener;
	}

	public interface DialogListener {
		public void triggered(File file);
	}

	public class SelectBackupRowAdapter extends ArrayAdapter<File> {
		public SelectBackupRowAdapter(Context context, File[] resource) {
			super(context, R.layout.dialog_select_campaign_fragment_row, resource);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			View view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);

			File file = getItem(position);
			TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			TextView text2 = (TextView) view.findViewById(android.R.id.text2);

			String format = Settings.System.getString(getActivity().getContentResolver(), Settings.System.DATE_FORMAT);
			String date;
			if (format == null || TextUtils.isEmpty(format)) {
				date = DateFormat.getDateFormat(getContext()).format(new Date(file.lastModified()));
				Log.w(LOGTAG, "There was no time settings found on the device! Choosing default!");
			} else {
				date = new SimpleDateFormat(format).format(new Date(file.lastModified())).replace("-", "/");
			}
			String name = file.getName();
			name = name.substring(0, name.length() - 4);

			text1.setText(String.format(getResources().getString(R.string.util_bold), name));
			text2.setText(String.format(getResources().getString(R.string.last_modified), date));

			return view;
		}
	}
}
