package de.wavegate.tos.dndpockettools.activity.mainfragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.colorizers.TableDataRowColorizer;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;
import de.wavegate.tos.dndpockettools.MainActivity;
import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.activity.view.PlayerCharacterDataAdapter;
import de.wavegate.tos.dndpockettools.activity.view.dialogfragment.NewCampaignFragment;
import de.wavegate.tos.dndpockettools.activity.view.dialogfragment.NewPlayerCharacterFragment;
import de.wavegate.tos.dndpockettools.activity.view.dialogfragment.SelectBackupFileFragment;
import de.wavegate.tos.dndpockettools.activity.view.dialogfragment.SelectExistingCampaignFragment;
import de.wavegate.tos.dndpockettools.activity.view.dialogfragment.ShareCampaignFragment;
import de.wavegate.tos.dndpockettools.activity.view.dialogfragment.ShowPartyAlignmentFragment;
import de.wavegate.tos.dndpockettools.data.Campaign;
import de.wavegate.tos.dndpockettools.data.PlayerCharacter;
import de.wavegate.tos.dndpockettools.util.DialogUtils;
import de.wavegate.tos.dndpockettools.util.FileManager;
import de.wavegate.tos.dndpockettools.util.Vignere;
import de.wavegate.tos.dndpockettools.util.error.ImportCampaignException;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;
import static de.wavegate.tos.dndpockettools.data.Campaign.CAPAIGN_VALUE_SEPERATOR;
import static de.wavegate.tos.dndpockettools.data.Campaign.CHARACTER_SEPERATOR;
import static de.wavegate.tos.dndpockettools.data.Campaign.CHARACTER_VALUE_SEPERATOR;

/**
 * Created by Nils on 02.04.2016.
 */
public class PartyStatsFragment extends MainMenuFragment implements TableDataClickListener<PlayerCharacter> {

	public static final String LAST_CAMPAIGN_NAME_TAG = MainActivity.PREFERENCES_TAG + "last_campaign_name";

	public static final String INSTANCE_EDIT_MODE = "edit_mode";

	private SortableTableView<PlayerCharacter> table;
	private TextView campaignNameTF;
	private Campaign activeCampaign;
	private boolean editMode;
	private View editPanel;
	private HashSet<PlayerCharacter> editModeSelection;
	private PlayerCharacterDataAdapter characterAdapter;
	private Button deleteBT, moveBT, copyBT;
	private int importCampaignSelection = 0;

	public PartyStatsFragment() {
		super();
	}

	public static int countChars(String s, char c) {
		int counter = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c) {
				counter++;
			}
		}
		return counter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.m_fragment_party_stats, container, false);

		table = (SortableTableView<PlayerCharacter>) v.findViewById(R.id.party_table_view);
		campaignNameTF = (TextView) v.findViewById(R.id.party_stats_campaign_name);
		deleteBT = (Button) v.findViewById(R.id.party_stats_delete_bt);
		moveBT = (Button) v.findViewById(R.id.party_stats_move_bt);
		copyBT = (Button) v.findViewById(R.id.party_stats_copy_bt);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		editModeSelection = new HashSet<>();

		table.setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());
		table.setDataRowColoriser(new CharTableColorizer());
		table.setColumnComparator(0, new CharacterNameCompactor());
		table.setColumnComparator(1, new CharacterPlayerCompactor());
		table.setColumnComparator(2, new CharacterXPCompactor());
		table.setColumnComparator(3, new CharacterXPCompactor());
		table.setColumnComparator(4, new CharacterAlignmentCompactor());
		table.addDataClickListener(this);

		table.setWillNotDraw(false);
		deleteBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onDeleteBT();
			}
		});
		moveBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onMoveBT();
			}
		});
		copyBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCopyBT();
			}
		});

		//deleteBT.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
		//moveBT.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
		//copyBT.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);

		editPanel = v.findViewById(R.id.party_stats_edit_panel);

		SimpleTableHeaderAdapter headerAdapter = new SimpleTableHeaderAdapter(getContext(), getString(R.string.table_name), getString(R.string.table_player), getString(R.string.table_XP), getString(R.string.table_Level), getString(R.string.table_Alignment));
		headerAdapter.setTextColor(Color.WHITE);
		table.setHeaderAdapter(headerAdapter);

		campaignNameTF.setText(R.string.party_stats_campaign_instructions);
		campaignNameTF.setClickable(true);
		campaignNameTF.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!editMode) {
					if (activeCampaign == null) {
						newCampaign();
					} else {
						loadCampaign();
					}
				} else {
					if (activeCampaign != null) {
						renameCampaign();
					}
				}
			}
		});

		String lastCampaign = preferences.getString(LAST_CAMPAIGN_NAME_TAG, "");
		if (!lastCampaign.equals("") && Campaign.getAllCampaignNames(getContext()).contains(lastCampaign)) {
			changeCampaign(lastCampaign);
		}

		editMode = false;
		if (savedInstanceState != null) {
			editMode = savedInstanceState.getBoolean(INSTANCE_EDIT_MODE, false);
		}
		applyEditMode();

		informMainActivity(getContext());
		updateTable();
		return v;
	}

	public void changeCampaign(Campaign campaign) {
		Log.i(LOGTAG, "Trying to change campaign to: " + campaign.getName());
		if (!campaign.exists(getContext())) {
			Log.i(LOGTAG, "Failed to load the requested campaign. Aborting!");
			Toast.makeText(getContext(), R.string.error_no_campaign_data, Toast.LENGTH_LONG).show();
			campaignNameTF.setText(R.string.party_stats_campaign_instructions);
			table.setVisibility(View.INVISIBLE);
			return;
		}

		//if (!campaign.hasValidNames(getContext())) {
		//	Log.w(LOGTAG, "Wanted to change the campaign. But campaign contained invalid Names! Applying auto-corrent and trying again.");
		//	campaign.makeNamesValid(getContext());
		//	changeCampaign(campaign);
		//} else Log.v(LOGTAG, "Checked the campaign's name and PC's name(s). Seems legid.");

		Log.i(LOGTAG, "Changing the campaign resulted in a success!");
		table.setVisibility(View.VISIBLE);
		campaignNameTF.setText(campaign.getName());
		activeCampaign = campaign;

		updateTable();
	}

	@Override
	public int getMenu() {
		if (!editMode)
			return R.menu.party_status_fragment;
		return R.menu.party_status_fragment_edit;
	}

	public void updateTable() {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
		ArrayList<PlayerCharacter> characters;
		if (activeCampaign == null) {
			campaignNameTF.setText(R.string.party_stats_campaign_instructions);
			characters = new ArrayList<PlayerCharacter>();
			editor.remove(LAST_CAMPAIGN_NAME_TAG);
		} else {
			characters = activeCampaign.getAll(getContext());
			campaignNameTF.setText(activeCampaign.getName());
			editor.putString(LAST_CAMPAIGN_NAME_TAG, activeCampaign.getName());
		}
		Log.i(LOGTAG, "Updating PlayerStats Table now. Chars: " + characters.size());

		editor.apply();
		characterAdapter = new PlayerCharacterDataAdapter(getContext(), characters);
		table.setDataAdapter(characterAdapter);
		table.invalidate();
	}

	public void toggleEditMode() {
		editMode = !editMode;
		applyEditMode();
	}

	private void applyEditMode() {
		if (editMode) {
			editPanel.setVisibility(View.VISIBLE);
			Toast.makeText(getContext(), R.string.info_select_characters_to_edit, Toast.LENGTH_LONG).show();
		} else {
			editPanel.setVisibility(View.GONE);
			editModeSelection.clear();
		}
		if (characterAdapter != null) {
			characterAdapter.notifyDataSetChanged();
		}
		table.invalidate();
		onTablePressedInEditMode();

		Activity mainActivity = getMainActivity();
		if (mainActivity != null) mainActivity.invalidateOptionsMenu();
	}

	@Override
	public boolean onBackPressed() {
		if (editMode) {
			toggleEditMode();
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_new_campaign:
				newCampaign();
				return true;
			case R.id.action_change_campaign:
				loadCampaign();
				return true;
			case R.id.action_add_new_character:
				addNewCharacter();
				return true;
			case R.id.action_awardXPToAll:
				awardXPToAll();
				return true;
			case R.id.action_show_party_alignment:
				showPartyAlignment();
				return true;
			case R.id.action_delete_all_characters:
				deleteAllCharacters();
				return true;
			case R.id.action_delete_this_campaign:
				deleteThisCampaign();
				return true;
			case R.id.action_cancel_edit:
				toggleEditMode();
				return true;
			case R.id.action_rename_this_campaign:
				renameCampaign();
				return true;
			case R.id.action_select_all:
				selectAll();
				return true;
			case R.id.action_share_campaign:
				share();
				return true;
			case R.id.action_import_campaign:
				showImportMenu();
				return true;
			case R.id.action_backupCampaign:
				onBackupCampaign("");
				return true;
			case R.id.action_editmode_party_status_fragment:
				Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
				if (v != null && v.hasVibrator())
					v.vibrate(25);
				toggleEditMode();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void deleteAllCharacters() {
		if (activeCampaign != null && activeCampaign.countPlayerCharacters(getContext()) > 1)
			return;
		new AlertDialog.Builder(getContext())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.app_name)
				.setMessage(R.string.actions_delete_all_characters_confirm)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activeCampaign.clearPlayers(getContext());
						updateTable();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	public void deleteThisCampaign() {
		if (activeCampaign == null) return;
		new AlertDialog.Builder(getContext())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.app_name)
				.setMessage(R.string.actions_delete_this_campaign_confirm)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activeCampaign.delete(getContext());
						activeCampaign = null;
						updateTable();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private void editCharacter(PlayerCharacter character) {
		if (activeCampaign == null) {
			return;
		}
		NewPlayerCharacterFragment fragment = new NewPlayerCharacterFragment();
		fragment.setCharacterToEdit(character);
		fragment.setListener(new NewPlayerCharacterFragment.DialogListener() {
			@Override
			public void triggered(PlayerCharacter character) {
				character.save(getContext(), activeCampaign);
				updateTable();
			}
		});
		fragment.show(getActivity().getSupportFragmentManager(), "new_create_character_dialog");

		updateTable();
	}

	private void addNewCharacter() {
		if (activeCampaign == null) {
			Toast.makeText(getContext(), R.string.error_no_campaign_to_add_people_to, Toast.LENGTH_LONG).show();
			return;
		}

		NewPlayerCharacterFragment fragment = new NewPlayerCharacterFragment();
		fragment.setListener(new NewPlayerCharacterFragment.DialogListener() {
			@Override
			public void triggered(PlayerCharacter character) {
				character.save(getContext(), activeCampaign);
				updateTable();
			}
		});
		fragment.show(getActivity().getSupportFragmentManager(), "new_create_character_dialog");

		updateTable();
	}

	@Override
	public int getTitle() {
		return R.string.drawer_category_party;
	}

	public void loadCampaign() {
		ArrayList<Campaign> campaigns = Campaign.loadAll(getContext());
		if (campaigns.size() == 0) {
			return;
		}

		selectCampaign(new SelectExistingCampaignFragment.DialogListener() {
			@Override
			public void triggered(Campaign campaign) {
				changeCampaign(campaign);
			}
		});
	}

	public void newCampaign() {
		NewCampaignFragment fragment = new NewCampaignFragment();
		fragment.setListener(new NewCampaignFragment.DialogListener() {
			@Override
			public void triggered(String name, String gmname) {
				Campaign campaign = new Campaign(name);
				campaign.setGMName(gmname);
				campaign.save(getContext());
				Log.i(LOGTAG, "I am triggered: " + name + " - " + gmname + ": changing now.");
				changeCampaign(campaign);
			}
		});

		fragment.show(getActivity().getSupportFragmentManager(), "new_create_campaign_dialog");
	}

	public void showPartyAlignment() {
		if (activeCampaign == null) return;
		ArrayList<PlayerCharacter> characters = activeCampaign.getAll(getContext());
		if (characters.size() == 0) return;

		ShowPartyAlignmentFragment fragment = new ShowPartyAlignmentFragment();
		fragment.setParty(characters);
		fragment.show(getActivity().getSupportFragmentManager(), "new_show_alignment_dialog");
	}

	private void share() {
		if (activeCampaign == null)
			return;

		if (!activeCampaign.hasValidNames(getContext())) {
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getContext());
			dlgAlert.setMessage(R.string.share_warning_illegal_characters);
			dlgAlert.setTitle(activeCampaign.getName());
			dlgAlert.setIcon(android.R.drawable.ic_dialog_alert);
			dlgAlert.setCancelable(true);
			dlgAlert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			dlgAlert.setPositiveButton(R.string.auto_resolve, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					activeCampaign.makeNamesValid(getContext());
					changeCampaign(activeCampaign);
					share();
				}
			});
			dlgAlert.create().show();
			return;
		}
		ShareCampaignFragment fragment = new ShareCampaignFragment();
		fragment.setCampaign(activeCampaign);
		fragment.show(getActivity().getSupportFragmentManager(), "new_share_campaign_dialog");
	}

	public void selectAll() {
		if (!editMode) return;

		if (editModeSelection.size() == characterAdapter.getData().size()) {
			editModeSelection.clear();
		} else {
			for (PlayerCharacter c : characterAdapter.getData()) {
				editModeSelection.add(c);
			}
		}
		onTablePressedInEditMode();
		characterAdapter.notifyDataSetChanged();
	}

	public void renameCampaign() {
		if (activeCampaign == null) return;

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.actions_rename_this_campaign);
		final EditText input = new EditText(getContext());
		input.setText(activeCampaign.getName());
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
		builder.setView(input);

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text = input.getText().toString().trim();
				if (!text.equals("")) {
					activeCampaign.rename(getContext(), input.getText().toString());
					updateTable();
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		builder.show();
	}

	public void onDeleteBT() {
		new AlertDialog.Builder(getContext()).setTitle(activeCampaign.getName()).setMessage(R.string.action_delete_confirm).setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (PlayerCharacter c : editModeSelection) {
					c.delete(getContext(), activeCampaign);
				}
				toggleEditMode();
				updateTable();
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();

	}

	public void onMoveBT() {
		Log.i(LOGTAG, "onMoveBT");
		selectCampaign(new SelectExistingCampaignFragment.DialogListener() {
			@Override
			public void triggered(Campaign campaign) {
				Log.i(LOGTAG, activeCampaign + " -> " + campaign);
				if (activeCampaign == campaign) {
					return;
					//TODO toast error
				}

				for (PlayerCharacter c : editModeSelection) {
					activeCampaign.moveCharacter(getContext(), c, campaign);
				}
				updateTable();
				toggleEditMode();
			}
		});
	}

	public void onCopyBT() {
		selectCampaign(new SelectExistingCampaignFragment.DialogListener() {
			@Override
			public void triggered(Campaign campaign) {
				if (activeCampaign == campaign) {
					Toast.makeText(getContext(), R.string.error_same_campaign, Toast.LENGTH_LONG).show();
					return;
				}
				for (PlayerCharacter c : editModeSelection) {
					campaign.addCharacter(getContext(), c);
				}
				updateTable();
				toggleEditMode();
			}
		});
	}

	public void awardXPToAll() {
		if (activeCampaign == null) return;
		final EditText numberTF = new EditText(getContext());
		numberTF.setInputType(InputType.TYPE_CLASS_NUMBER);
		numberTF.setText("0");

		DialogUtils.textInput(getContext(), R.string.action_warward_xp_to_all, numberTF, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text = numberTF.getText().toString();
				int xp = 0;
				try {
					xp = Integer.parseInt(text);
				} catch (NumberFormatException e) {
					Log.w(LOGTAG, "Invalid input.");
					Toast.makeText(getContext(), R.string.error_invalid_input, Toast.LENGTH_LONG).show();
					return;
				}

				for (PlayerCharacter character : activeCampaign.getAll(getContext())) {
					character.modXP(xp);
					character.save(getContext(), activeCampaign);
				}
				updateTable();
			}
		});
	}

	private void showImportMenu() {
		//ArrayList<String> list = new ArrayList<>();
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list);
		//ListView view = new ListView(getContext());
		//view.setAdapter(adapter);
		importCampaignSelection = 0;

		new AlertDialog.Builder(getContext())
				.setTitle(R.string.actions_import_campaign_info)
				//.setMessage(R.string.actions_import_campaign_info)
				.setSingleChoiceItems(R.array.import_campaign_options, importCampaignSelection, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						importCampaignSelection = which;
					}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (importCampaignSelection) {
							case 1:
								importViaText();
								break;
							case 0:
								importViaQR();
								break;
							case 2:
								importViaBackup();
								break;
							default:
								Log.e(LOGTAG, "Wanted to import a campaign. But dialog resulted in an invalid selection: " + importCampaignSelection);
								break;
						}
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				//.setIcon(android.R.drawable.ic_dialog_info)
				.show();
	}

	private void onTablePressedInEditMode() {
		moveBT.setEnabled(!editModeSelection.isEmpty());
		copyBT.setEnabled(!editModeSelection.isEmpty());
		deleteBT.setEnabled(!editModeSelection.isEmpty());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_EDIT_MODE, editMode);
	}

	public void changeCampaign(String campaign) {
		changeCampaign(new Campaign(campaign));
	}

	@Override
	public void onDataClicked(int rowIndex, PlayerCharacter clickedData) {
		Log.i(LOGTAG, "A character at row " + rowIndex + " was selected: " + clickedData);
		if (editMode) {
			if (editModeSelection.contains(clickedData))
				editModeSelection.remove(clickedData);
			else editModeSelection.add(clickedData);

			onTablePressedInEditMode();

			if (characterAdapter != null)
				characterAdapter.notifyDataSetChanged();
			Log.i(LOGTAG, "Edit mode selected characters: " + editModeSelection.size());
		} else
			editCharacter(clickedData);
	}

	private void selectCampaign(SelectExistingCampaignFragment.DialogListener listener) {
		//SelectExistingCampaignFragment.DialogListener listener = new SelectExistingCampaignFragment.DialogListener() {
		//	@Override
		//	public void triggered(Campaign campaign) {
		//		changeCampaign(campaign);
		//	}
		//};
		SelectExistingCampaignFragment fragment = new SelectExistingCampaignFragment();
		fragment.setListener(listener);
		fragment.show(getActivity().getSupportFragmentManager(), "new_choose_campaign_dialog");
	}

	private void importViaQR() {
		//try {
		//	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		//	intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
		//	startActivityForResult(intent, 0);
		//} catch (Exception e) {
		//	Log.w(LOGTAG, "Exception when trying to start ReadQR intent on the first try!", e);
		//	Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
		//	Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
		//	startActivity(marketIntent);
		//}

		IntentIntegrator integrator = new IntentIntegrator(getActivity());
		integrator.setPrompt(getString(R.string.actions_import_campaign));
		integrator.setBeepEnabled(true);
		integrator.initiateScan();
	}

	private void importViaBackup() {
		SelectBackupFileFragment fragment = new SelectBackupFileFragment();
		fragment.setListener(new SelectBackupFileFragment.DialogListener() {
			@Override
			public void triggered(File file) {
				Log.i(LOGTAG, "Selected file: " + file.getAbsolutePath());
				ImportCampaignResults results = null;
				try {
					results = importViaBackup(file);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(LOGTAG, "Wanted to import via backup, but got an Exception!", e);
					Toast.makeText(getContext(), "This is a debug error text: " + e.getClass().getCanonicalName() + ": '" + e.getMessage() + "'", Toast.LENGTH_LONG).show();
					//TODO display error toast
				}
				if (results != null) {
					tryToSaveImportedCampaign(results);
				}
			}
		});
		fragment.show(getActivity().getSupportFragmentManager(), "new_choose_backup_dialog");
	}

	private ImportCampaignResults importViaBackup(File file) throws IOException {
		Log.i(LOGTAG, "About to import campaign data from '" + file.getAbsolutePath() + "' Exists? " + file.exists());
		Properties properties = new Properties();
		properties.loadFromXML(new FileInputStream(file));

		Log.i(LOGTAG, "Data found in file: " + properties.stringPropertyNames());
		ImportCampaignResults results = null;
		try {
			results = importViaBackup(properties);
		} catch (ImportCampaignException e) {
			String error = e.getMessage();
			Log.e(LOGTAG, "Encountered an 'missing-Properties' error while importing from Properties: '" + error + "'", e);
			Toast.makeText(getContext(), String.format(getResources().getString(R.string.error_backup_loading_error), error), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(LOGTAG, "Encountered an unexpected error while importing from Properties: '" + e.getMessage() + "'", e);
			e.printStackTrace();
			//TODO error message
		}
		Log.i(LOGTAG, "Information in the file are valid.");
		return results;
	}

	private ImportCampaignResults importViaBackup(Properties properties) throws ImportCampaignException {
		String campaignName = properties.getProperty(Campaign.TAG_NAME);
		if (campaignName == null || TextUtils.isEmpty(campaignName))
			throw new ImportCampaignException(R.string.error_missing_campaign_name, getContext());

		String gmName = properties.getProperty(Campaign.TAG_GM_NAME);
		if (gmName == null || TextUtils.isEmpty(gmName))
			throw new ImportCampaignException(R.string.error_missing_campaign_gm_name, getContext());

		String playerCountSt = properties.getProperty(Campaign.TAG_PROPERTIES_CHARACTER_COUNT);
		if (playerCountSt == null || TextUtils.isEmpty(playerCountSt) || !TextUtils.isDigitsOnly(playerCountSt))
			throw new ImportCampaignException(R.string.error_missing_campaign_player_count, getContext());

		for (int i = 0; i < Integer.parseInt(playerCountSt); i++) {
			String tag = PlayerCharacter.getToMapBaseTag(i);

			String tag_ac = tag + PlayerCharacter.TAG_AC;
			String tag_name = tag + PlayerCharacter.TAG_NAME;
			String tag_notes = tag + PlayerCharacter.TAG_NOTES;
			String tag_playerName = tag + PlayerCharacter.TAG_PLAYER_NAME;
			String tag_xp = tag + PlayerCharacter.TAG_XP;
			String tag_alignment = tag + PlayerCharacter.TAG_ALIGNMENT;

			ArrayList<String> tagList = new ArrayList<>();
			tagList.add(tag_ac);
			tagList.add(tag_name);
			tagList.add(tag_notes);
			tagList.add(tag_playerName);
			tagList.add(tag_xp);
			tagList.add(tag_alignment);

			for (String t : tagList) {
				if (properties.getProperty(t) == null) {
					Log.e(LOGTAG, "Tag '" + t + "' was not found for Character " + i + "! Proof: " + properties.getProperty(t));
					throw new ImportCampaignException(String.format(getResources().getString(R.string.error_backup_missing_player_info), String.valueOf(i)));
				}
			}
			Log.i(LOGTAG, "Every parameter for Character " + i + " present if backup file.");

			String name = properties.getProperty(tag_name);
			String notes = properties.getProperty(tag_notes);
			String playerName = properties.getProperty(tag_playerName);

			int ac, xp, alignment;
			try {
				ac = Integer.parseInt(properties.getProperty(tag_ac));
				xp = Integer.parseInt(properties.getProperty(tag_ac));
				alignment = Integer.parseInt(properties.getProperty(tag_ac));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw new ImportCampaignException(String.format(getResources().getString(R.string.error_backup_missing_player_info), String.valueOf(i)));
			}

			PlayerCharacter character = new PlayerCharacter(name, 0, ac, xp, playerName, alignment);
			character.setNotes(notes);
		}

		Set<PlayerCharacter> characters = new HashSet<>();
		Campaign campaign = new Campaign(campaignName);
		campaign.setGMName(gmName);

		return new ImportCampaignResults(campaign, characters);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0) {
			if (resultCode == Activity.RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");
				contents = new Vignere().vignereCrypt(contents, Campaign.TAG, false);
				Log.i(LOGTAG, "I read a QR Code: " + contents);
				importViaText(contents, true);
			}
		}
	}

	private void importViaText() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.actions_import_campaign);

		final EditText input = new EditText(getContext());
		//input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		builder.setView(input);

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text = input.getText().toString().trim();
				Log.i(LOGTAG, "Raw input: " + text);
				text = new Vignere().vignereCrypt(text, Campaign.TAG, false);
				importViaText(text, false);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		builder.show();
	}

	private void onBackupCampaign(String filename) {
		if (activeCampaign == null) return; //TODO display error message
		final EditText input = new EditText(getContext());
		input.setText("");
		if (filename != null && !filename.equals("")) {
			input.setText(filename);
		}
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.action_backup_campaign).
				setView(input).
				setMessage(R.string.backup_name).
				setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String filename = input.getText().toString().trim();
						File file = getBackupFile(filename);

						Log.i(LOGTAG, "Map: " + activeCampaign.toProperties(getContext()) + " -> " + file.getAbsolutePath() + " exists? " + file.exists());

						if (file.exists()) {
							Toast.makeText(getContext(), R.string.error_duplicate_backup_name, Toast.LENGTH_LONG).show();
							onBackupCampaign(input.getText().toString());
							return;
						}
						try {
							backupCampaign(file);
						} catch (IOException e) {
							e.printStackTrace();
							//TODO handle exception
						}
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		builder.show();
	}

	private void backupCampaign(File targetFile) throws IOException {
		if (!targetFile.exists()) {
			if (!targetFile.createNewFile()) {
				throw new IOException("Could not create that file.");
			}
		}

		Properties properties = activeCampaign.toProperties(getContext());
		OutputStream stream = new FileOutputStream(targetFile);
		properties.storeToXML(stream, null);
		Toast.makeText(getContext(), R.string.backup_success, Toast.LENGTH_LONG).show();
	}

	private void importViaText(String text, boolean fromQR) {
		ImportCampaignResults results = null;
		try {
			results = interpretText(text);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(LOGTAG, "Could not interpret '" + text + "' correctly: " + e.getMessage(), e);
		}
		if (results == null) {
			String errorText = getContext().getResources().getString(R.string.actions_import_campaign_error_wrong_text);
			if (fromQR)
				errorText = getContext().getResources().getString(R.string.actions_import_campaign_error_wrong_code);
			new AlertDialog.Builder(getContext())
					.setTitle(R.string.actions_import_campaign)
					.setMessage(errorText)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
			return;
		}

		tryToSaveImportedCampaign(results);
	}

	private void tryToSaveImportedCampaign(ImportCampaignResults results) {
		Campaign c = results.getCampaign();
		if (c.exists(getContext())) {
			Log.w(LOGTAG, "That campaign already exists! Actions will be taken...");
			final ImportCampaignResults finalResults = results;
			new AlertDialog.Builder(getContext())
					.setTitle(c.getName())
					.setMessage(R.string.actions_import_campaign_exists_warning)
					.setPositiveButton(R.string.create_new, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							int tries = 0;
							Campaign campaign = finalResults.getCampaign();
							String originName = campaign.getName();

							while (campaign.exists(getContext())) {
								tries++;
								campaign = new Campaign(originName + "(" + tries + ")");
								campaign.setGMName(finalResults.getCampaign().getGMName());
							}
							saveImportedCampaign(new ImportCampaignResults(campaign, finalResults.getCharacters()));
						}
					})
					.setNeutralButton(R.string.integrate, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							new Campaign(finalResults.getCampaign().getName()).delete(getContext());
							saveImportedCampaign(finalResults);
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setCancelable(false)
					.show();
		} else {
			saveImportedCampaign(results);
		}
	}

	private void saveImportedCampaign(ImportCampaignResults results) {
		Campaign c = results.getCampaign();
		Set<PlayerCharacter> characters = results.getCharacters();

		c.save(getContext());
		for (PlayerCharacter playerCharacter : characters)
			playerCharacter.save(getContext(), c);

		changeCampaign(c);
	}

	private ImportCampaignResults interpretText(String text) throws IllegalArgumentException {
		Log.i(LOGTAG, "Will interpret this text into campaign data: " + text);
		if (countChars(text, CAPAIGN_VALUE_SEPERATOR) != 2)
			throw new IllegalArgumentException("The amount of '" + CAPAIGN_VALUE_SEPERATOR + "' in the code is incorrect!");

		int pos = text.indexOf(CAPAIGN_VALUE_SEPERATOR);
		String campaignName = text.substring(0, pos);
		text = text.substring(pos + 1);

		pos = text.indexOf(CAPAIGN_VALUE_SEPERATOR);
		String gmName = text.substring(0, pos);
		text = text.substring(pos + 1);

		Log.i(LOGTAG, "Campaign: " + campaignName + " GM: " + gmName);
		Set<String> characterCodes = new HashSet<>();
		while (text.startsWith(String.valueOf(CHARACTER_SEPERATOR)) && text.length() != 0) {
			//Log.i(LOGTAG,"t1: "+text);
			text = text.substring(1);
			//Log.i(LOGTAG,"t2: "+text);
			pos = text.indexOf(CHARACTER_SEPERATOR);
			characterCodes.add(text.substring(0, pos));
			text = text.substring(pos + 1);
			//Log.i(LOGTAG,"t3: "+text);
			//Log.v(LOGTAG, "Another while bites the dust... " + characterCodes);
		}
		Log.i(LOGTAG, "Found characters: " + characterCodes + " Count: " + characterCodes.size() + " Resulting text: '" + text + "'");
		if (text.length() != 0)
			throw new IllegalArgumentException("After interpreting all the characters there was still text left. Found: " + characterCodes + " text left: " + text);

		Pattern characterPattern = Pattern.compile("\\" + CHARACTER_VALUE_SEPERATOR + "(\\w*)\\" + CHARACTER_VALUE_SEPERATOR + "(\\d+)\\" + CHARACTER_VALUE_SEPERATOR + "(\\w*)\\" + CHARACTER_VALUE_SEPERATOR + "(\\d+)\\" + CHARACTER_VALUE_SEPERATOR + "");
		Log.i(LOGTAG, "Regex Matcher for individual Characters: " + characterPattern.pattern());
		Set<PlayerCharacter> playerCharacters = new HashSet<>();
		for (String characterCode : characterCodes) {
			Matcher m = characterPattern.matcher(characterCode);
			if (m.matches()) {
				String charName = m.group(1);
				int xp = Integer.parseInt(m.group(2));
				String playerName = m.group(3);
				int alignment = Integer.parseInt(m.group(4));

				playerCharacters.add(new PlayerCharacter(charName, 0, 0, xp, playerName, alignment));
			}
		}
		if (characterCodes.size() != playerCharacters.size())
			throw new IllegalArgumentException("The amount of characters does not match the amount of interpreted codes! Codes: " + characterCodes.size() + " Created Characters: " + playerCharacters.size());

		Campaign campaign = new Campaign(campaignName);
		campaign.setGMName(gmName);

		return new ImportCampaignResults(campaign, playerCharacters);
	}

	private File getBackupFile(String filename) {
		FileManager manager = new FileManager(getContext());
		File dir = manager.getCampaignBackupDir();

		filename = filename.trim();
		if (filename.equals("")) filename = getResources().getString(R.string.error_unknown);
		if (filename.endsWith(".")) filename = filename + "xml";
		else filename = filename + ".xml";

		return new File(dir, filename);
	}

	private class ImportCampaignResults {
		private Campaign campaign;
		private Set<PlayerCharacter> characters;

		public ImportCampaignResults(Campaign campaign, Set<PlayerCharacter> characters) {
			this.campaign = campaign;
			this.characters = characters;
		}

		public Campaign getCampaign() {
			return campaign;
		}

		//public void setCampaign(Campaign campaign) {
		//	this.campaign = campaign;
		//}

		public Set<PlayerCharacter> getCharacters() {
			return characters;
		}

		//public void setCharacters(Set<PlayerCharacter> characters) {
		//	this.characters = characters;
		//}
	}

	private class CharTableColorizer implements TableDataRowColorizer<PlayerCharacter> {

		public static final int COLOR_ORANGE = 0xFFFF8080;

		@Override
		public int getRowColor(int rowIndex, PlayerCharacter rowData) {
			Log.i(LOGTAG, "changing color now for row " + rowIndex + " editmode? " + editMode);
			if (editMode) {
				if (editModeSelection.contains(rowData)) {
					if (rowIndex % 2 == 0) return COLOR_ORANGE;
					return Color.RED;
				}
			}

			if (rowIndex % 2 == 0) return Color.WHITE;
			return Color.LTGRAY;
		}
	}

	private class CharacterNameCompactor implements Comparator<PlayerCharacter> {
		@Override
		public int compare(PlayerCharacter lhs, PlayerCharacter rhs) {
			return lhs.getName().compareTo(rhs.getName());
		}
	}

	private class CharacterPlayerCompactor implements Comparator<PlayerCharacter> {
		@Override
		public int compare(PlayerCharacter lhs, PlayerCharacter rhs) {
			return lhs.getPlayerName().compareTo(rhs.getPlayerName());
		}
	}

	private class CharacterXPCompactor implements Comparator<PlayerCharacter> {
		@Override
		public int compare(PlayerCharacter lhs, PlayerCharacter rhs) {
			return lhs.getXP() - rhs.getXP();
		}
	}

	private class CharacterAlignmentCompactor implements Comparator<PlayerCharacter> {
		@Override
		public int compare(PlayerCharacter lhs, PlayerCharacter rhs) {
			return lhs.getAlignment() - rhs.getAlignment();
		}
	}
}