package de.wavegate.tos.dndpockettools.activity.mainfragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.activity.fragment.NameGeneratorListFragment;
import de.wavegate.tos.dndpockettools.activity.view.dialogfragment.MarkovSelectLookaheadFragment;
import de.wavegate.tos.dndpockettools.data.Campaign;
import de.wavegate.tos.dndpockettools.data.PlayerCharacter;
import de.wavegate.tos.dndpockettools.data.hmm.DefaultNameLists;
import de.wavegate.tos.dndpockettools.data.hmm.MarkovResults;
import de.wavegate.tos.dndpockettools.data.hmm.MarkovTraining;
import de.wavegate.tos.dndpockettools.util.DialogUtils;
import de.wavegate.tos.dndpockettools.util.FileManager;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 22.05.2016.
 */
public class NameGeneratorFragment extends MainMenuFragment implements ViewPager.OnPageChangeListener {

	public static final String NAME_GENERATOR_SOURCE_EXTENSION = ".txt";
	public static final String PREFERENCES_GENERATION_AMOUNT = "markov_generation_amount";
	//public static final String PREFERENCES_MARKOV_INFO_DONT_SHOW_AGAIN = "markov_generation_info_dont_show_again";
	public static final String PRFERENCES_MARKOV_DISPLAY_INFO_TAB = "markov_display_info_tab";
	public static final String PRFERENCES_MARKOV_FIRST_TIME_VISITED = "markov_first_time_visited";

	public static final int DEFAULT_GENERATION_AMOUNT = 15;
	public static final int MAX_NAME_GENERATED_COUNT = 250;
	public static final String PREFERENCES_CUSTOM_MARKOV_LOOKAHEAD = "preferences_custom_markov_lookahead";
	public static final int MAX_CALCULATED_LOOKAHEAD = 3;
	private ArrayList<MarkovResults> resultsList;
	private Toast pagerToast;
	private SectionsPagerAdapter sectionsPagerAdapter;
	private ViewPager viewPager;
	private MarkovTrainingTask activeTrainingTask;
	private Toast errorToast;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		viewPager = (ViewPager) inflater.inflate(R.layout.m_fragment_name_generator, container, false);
		try {
			resultsList = getFileList();
		} catch (IOException e) {
			e.printStackTrace();
			//TODO handle exception
			resultsList = new ArrayList<>();
		}
		Collections.sort(resultsList);
		boolean resultsListEmpty = resultsList.isEmpty();

		if (!Campaign.loadAll(getContext()).isEmpty()) {
			MarkovTraining training = new MarkovTraining(getContext().getResources().getString(R.string.markov_tab_all_player_characters));
			for (Campaign campaign : Campaign.loadAll(getContext())) {
				for (PlayerCharacter playerCharacter : campaign.getAll(getContext())) {
					training.add(playerCharacter.getName());
				}
			}
			resultsList.add(new MarkovResults(training, getContext()));
		}

		sectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager(), resultsList);
		viewPager.setAdapter(sectionsPagerAdapter);
		viewPager.addOnPageChangeListener(this);

		Log.i(LOGTAG, "NameGeneratorFragment - onCreate() was called");

		informMainActivity(getContext());
		updateViewPager();

		if (getPreferences().getBoolean(PRFERENCES_MARKOV_FIRST_TIME_VISITED, true)) {
			boolean success = false;
			Collections.reverse(resultsList);
			try {
				createDefaultReferences();
				success = true;
			} catch (IOException e) {
				e.printStackTrace();
				Log.i(LOGTAG, "Failed to create default references.", e);
				//TODO handle error
			}
			Collections.reverse(resultsList);
			if (success) {
				SharedPreferences.Editor editor = getPreferences().edit();
				editor.putBoolean(PRFERENCES_MARKOV_FIRST_TIME_VISITED, false);
				editor.putBoolean(PRFERENCES_MARKOV_DISPLAY_INFO_TAB, true);
				editor.apply();
			}
		}
		loadMarkovAsynchronous();

		return viewPager;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_next_markov:
				nextMarkov();
				return true;
			case R.id.action_clear_markov:
				actionClearMarkov();
				return true;
			case R.id.action_clear_markov_all:
				actionClearMarkovAll();
				return true;
			case R.id.action_markov_information:
				actionShowMarkovInfo();
				return true;
			case R.id.action_markov_generation_amount:
				actionChangeGenerationAmount();
				return true;
			case R.id.action_markov_create_defaults:
				actionCreateDefault();
				return true;
			case R.id.action_select_markov_training_element:
				actionDisplayRandomTrainingData();
				return true;
			case R.id.action_quick_select_markov:
				actionQuickSelect();
				return true;
			case R.id.action_markov_select_lookahead:
				actionSelectLookahead();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void actionSelectLookahead() {
		MarkovSelectLookaheadFragment fragment = new MarkovSelectLookaheadFragment();
		fragment.setListener(new MarkovSelectLookaheadFragment.Listener() {
			@Override
			public void triggered(int mode) {
				SharedPreferences.Editor editor = getPreferences().edit();
				editor.putInt(PREFERENCES_CUSTOM_MARKOV_LOOKAHEAD, mode);
				editor.apply();
				Log.i(LOGTAG, "Markov Lookahead: Fragment listener get: " + mode);
			}
		});

		fragment.show(getActivity().getSupportFragmentManager(), "new_markov_lookahead_frame");
	}

	public void actionChangeGenerationAmount() {
		int current_amount = getPreferences().getInt(PREFERENCES_GENERATION_AMOUNT, DEFAULT_GENERATION_AMOUNT);
		DialogUtils.NumberPickerListener listener = new DialogUtils.NumberPickerListener() {
			@Override
			public void triggered(DialogInterface dialog, int which, int selectedValue) {
				dialog.dismiss();
				SharedPreferences.Editor editor = getPreferences().edit();
				editor.putInt(PREFERENCES_GENERATION_AMOUNT, selectedValue);
				editor.apply();
			}
		};
		DialogUtils.selectNumber(getContext(), R.string.markov_change_generation_amount_detail, 1, MAX_NAME_GENERATED_COUNT, current_amount, listener);
	}

	@Override
	public int getMenu() {
		if (getContext() == null) {
			return R.menu.name_generator_fragment;
		}
		if (isShowInfoTab() && isInfoTabSelected()) return R.menu.empty;
		return R.menu.name_generator_fragment;
	}

	public void actionShowMarkovInfo() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.app_name);
		builder.setIcon(android.R.drawable.ic_dialog_info);

		builder.setMessage(R.string.markov_info_detail);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		//if (!getPreferences().getBoolean(PREFERENCES_MARKOV_INFO_DONT_SHOW_AGAIN, false)) {
		//	builder.setNeutralButton(R.string.ok_dont_show_again, new DialogInterface.OnClickListener() {
		//		@Override
		//		public void onClick(DialogInterface dialog, int which) {
		//			dialog.dismiss();
		//			SharedPreferences.Editor editor = getPreferences().edit();
		//			editor.putBoolean(PREFERENCES_MARKOV_INFO_DONT_SHOW_AGAIN, true);
		//			editor.apply();
		//		}
		//	});
		//}
		builder.setNeutralButton(R.string.show_scientific_explanation, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showScientificExplanation();
			}
		});
		builder.setNegativeButton(R.string.action_create_example_resources, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				actionCreateDefault();
			}
		});

		builder.show();
	}

	public void showScientificExplanation() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.show_scientific_explanation);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setNeutralButton(R.string.browse_directory, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				FileManager manager = new FileManager(getContext());
				manager.browseFolder(manager.getNameGeneratorSources());
			}
		});
		String path = new FileManager(getContext()).getNameGeneratorSources().getPath();
		builder.setMessage(String.format(getString(R.string.markiv_info_scientific_detail), NAME_GENERATOR_SOURCE_EXTENSION, path));
		builder.show();
	}

	public void actionDisplayRandomTrainingData() {
		if (!checkMarkovAvailable()) {
			return;
		}
		if (errorToast == null) {
			errorToast = Toast.makeText(getContext(), "", Toast.LENGTH_LONG);
		}
		errorToast.setDuration(Toast.LENGTH_LONG);
		errorToast.setText(getRandomTrainingData());
		errorToast.show();
	}

	public String getRandomTrainingData() {
		MarkovTraining training = resultsList.get(getSelectedPage()).getTraining();
		if (training != null && training.size() != 0)
			return training.getTrainingData(new Random().nextInt(training.size()));
		return "";
	}

	private void actionCreateDefault() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.action_create_example_resources);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setPositiveButton(R.string.action_create_example_resources_short, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					createDefaultReferences();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(LOGTAG, "Could not create the Markov example file: " + new DefaultNameLists(getContext()).getDnd5MarkovNameFile().getAbsolutePath(), e);
					//TODO show error message
				}
			}
		});
		builder.setNeutralButton(R.string.action_check_example, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				browseDnD5eFile();
			}
		});

		String path = new FileManager(getContext()).getNameGeneratorSources().getPath();
		builder.setMessage(String.format(getString(R.string.action_markov_create_defaults_extended), NAME_GENERATOR_SOURCE_EXTENSION, path));

		builder.show();
	}

	private void browseDnD5eFile() {
		File f = new DefaultNameLists(getContext()).getDnd5MarkovNameFile();
		if (!f.exists()) {
			Toast.makeText(getContext(), R.string.error_example_file_not_found, Toast.LENGTH_LONG).show();
			return;
		}

		Intent i = new Intent();
		i.setAction(android.content.Intent.ACTION_VIEW);
		i.setDataAndType(Uri.fromFile(f), " text/*");
		startActivity(i);
	}

	private void createDefaultReferences() throws IOException {
		DefaultNameLists defaultNameLists = new DefaultNameLists(getContext());
		File file = defaultNameLists.getDnd5MarkovNameFile();
		if (file.exists()) {
			Toast.makeText(getContext(), R.string.error_markov_default_file_exists, Toast.LENGTH_LONG).show();
			return;
		}

		boolean b = file.createNewFile();
		if (!b) throw new IOException("File#createNewFil() returned false");

		ArrayList<String> nameList = defaultNameLists.createDnD5Names();
		FileWriter fw = new FileWriter(file, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);

		for (int i = 0; i < nameList.size(); i++) {
			out.println(nameList.get(i));
		}

		bw.close();

		MarkovTraining training = new MarkovTraining(getString(R.string.filename_dnd5_gm_screen_names));
		resultsList.add(new MarkovResults(training, getContext()));

		loadMarkovAsynchronous();
	}

	private int getSelectedLookahead() {
		int i = getPreferences().getInt(PREFERENCES_CUSTOM_MARKOV_LOOKAHEAD, 0);
		Log.i(LOGTAG, "Markov Lookahead preference: " + i);
		if (i != 0) return i;

		i = getCurrentResults().getTraining().size() / 500 + 1;
		Log.i(LOGTAG, "Markov Lookahead calculated: " + i);
		return Math.min(i, MAX_CALCULATED_LOOKAHEAD);
	}

	public void loadMarkovAsynchronous() {
		FileManager manager = new FileManager(getContext());
		MarkovTrainingTaskElement[] elements = new MarkovTrainingTaskElement[resultsList.size()];
		for (int i = 0; i < resultsList.size(); i++) {
			MarkovTraining training = resultsList.get(i).getTraining();
			File file = new File(manager.getNameGeneratorSources(), training.getName() + NAME_GENERATOR_SOURCE_EXTENSION);
			MarkovTrainingTaskElement element = new MarkovTrainingTaskElement(training, file);
			elements[i] = element;
		}

		activeTrainingTask = new MarkovTrainingTask();
		activeTrainingTask.execute(elements);
	}

	public boolean checkMarkovAvailable() {
		if (errorToast == null) {
			errorToast = Toast.makeText(getContext(), "", Toast.LENGTH_LONG);
		}

		if (isShowInfoTab() && getSelectedPage() == resultsList.size()) {
			errorToast.setText(R.string.error_markov_cant_do_that_info_page);
			errorToast.show();
			return false;
		}

		if (!(activeTrainingTask != null && activeTrainingTask.getStatus() == AsyncTask.Status.FINISHED)) {
			errorToast.setText(R.string.error_markov_not_loaded_yet);
			errorToast.show();
			return false;
		}

		return true;
	}

	public void nextMarkov() {
		int count = getPreferences().getInt(PREFERENCES_GENERATION_AMOUNT, DEFAULT_GENERATION_AMOUNT);
		int alreadyCreated = 0;
		try {
			alreadyCreated = getCurrentResults().getResults().size();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (alreadyCreated >= MAX_NAME_GENERATED_COUNT) {
			Toast.makeText(getContext(), R.string.error_maximum_markov_reached, Toast.LENGTH_LONG).show();
			return;
		}
		if (count + alreadyCreated > MAX_NAME_GENERATED_COUNT) {
			count = MAX_NAME_GENERATED_COUNT - alreadyCreated;
		}
		nextMarkov(count);
	}

	private void nextMarkov(int count) {
		MarkovResults results = getCurrentResults();
		try {
			results.nextMarkov(count, getSelectedLookahead());
			Log.i(LOGTAG, "Currently available MarkovResults: " + results.getResults());
			Log.i(LOGTAG, "Currently created HMMs: " + results.getResults().size());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(LOGTAG, "Could not add more NextMarkovs!", e);
		}
		//Toast.makeText(getContext(), "Chosen lookahead: " + getSelectedLookahead(), Toast.LENGTH_LONG).show();

		Log.i(LOGTAG, "Notifiying my PagerAdapter about the latest changes!");
		updateViewPager();
	}

	@Override
	public int getTitle() {
		return R.string.drawer_category_name_generator;
	}

	@Override
	public boolean getFABVisible() {
		return getContext() == null || !(isShowInfoTab() && isInfoTabSelected());
	}

	@Override
	public void onFABClicked() {
		if (!checkMarkovAvailable()) {
			return;
		}
		nextMarkov();
	}

	@Override
	public int getFABIcon() {
		return R.drawable.ic_action_new;
	}

	private void clearMarkov(int position) {
		try {
			resultsList.get(position).clear();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(LOGTAG, "Could not clear the content of NameGenerator Cache File, related to position " + position, e);
			//TODO show error message
		}
		updateViewPager();
	}

	public void actionClearMarkov() {
		if (!checkMarkovAvailable()) {
			return;
		}
		clearMarkov(getSelectedPage());
	}

	public void actionQuickSelect() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		//getLayoutInflater(null).inflate(android.R.layout.simple_list_item_2, null);

		ArrayAdapter adapter = new ArrayAdapter<MarkovResults>(getContext(), android.R.layout.simple_list_item_2, resultsList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				//LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				convertView = getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
				MarkovResults results = getItem(position);
				TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
				TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
				text1.setText(results.getName());
				text2.setText(String.format(getString(R.string.markov_training_count), String.valueOf(results.getTraining().size())));
				//TextView v = new TextView(getContext());
				//v.setText("nils ist cool");
				return convertView;
			}
		};
		ListView listView = new ListView(getContext());
		listView.setAdapter(adapter);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setView(listView);
		final AlertDialog dialog = builder.show();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				viewPager.setCurrentItem(position, true);
				dialog.dismiss();
			}
		});
	}

	public boolean isShowInfoTab() {
		return getPreferences().getBoolean(PRFERENCES_MARKOV_DISPLAY_INFO_TAB, true);
	}

	public boolean isInfoTabSelected() {
		return isShowInfoTab() && getSelectedPage() == viewPager.getAdapter().getCount() - 1;
	}

	public void actionClearMarkovAll() {
		if (!checkMarkovAvailable()) {
			return;
		}
		for (int i = 0; i < resultsList.size(); i++)
			clearMarkov(i);
	}

	@Override
	public void onResume() {
		super.onResume();
		updateViewPager();
	}

	@Override
	public void onPause() {
		super.onPause();
		activeTrainingTask.cancel(true);
	}

	@Override
	public void onStop() {
		super.onStop();
		activeTrainingTask.cancel(true);
	}

	public MarkovResults getCurrentResults() {
		return resultsList.get(getSelectedPage());
	}

	public NameGeneratorListFragment getActiveFragment() {
		Log.i(LOGTAG, "Pager current selected: " + viewPager.getChildAt(getSelectedPage()).getClass().getName());
		return sectionsPagerAdapter.getItem(getSelectedPage());
	}

	public ArrayList<MarkovResults> getFileList() throws IOException {
		ArrayList<MarkovResults> list = new ArrayList<>();
		List<File> files = new LinkedList<>();
		File nameDir = new FileManager(getContext()).getNameGeneratorSources();
		for (File f : nameDir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(NAME_GENERATOR_SOURCE_EXTENSION))
				files.add(f);
		}

		for (File f : files) {
			MarkovTraining training = new MarkovTraining(f.getName().substring(0, f.getName().length() - NAME_GENERATOR_SOURCE_EXTENSION.length()));
			//trainMarkov(training, f);
			list.add(new MarkovResults(training, getContext()));
		}

		return list;
	}

	public int getSelectedPage() {
		return viewPager.getCurrentItem();
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		//updateViewPager();
		if (pagerToast == null) {
			pagerToast = Toast.makeText(getContext(), R.string.info_page, Toast.LENGTH_LONG);
		}
		getMainActivity().invalidateFragmentUI();
		Log.i(LOGTAG, "Page: " + getSelectedPage() + " -> InfoTab? " + isInfoTabSelected());
		pagerToast.setText(String.format(getString(R.string.info_page), String.valueOf(getSelectedPage() + 1), String.valueOf(sectionsPagerAdapter.getCount())));
		pagerToast.show();
	}

	public void updateViewPager() {
		if (getActivity() != null && !getActivity().isDestroyed() && sectionsPagerAdapter != null)
			sectionsPagerAdapter.notifyDataSetChanged();
	}

	@Override
	public void onStart() {
		super.onStart();
		updateViewPager();
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private ArrayList<MarkovResults> list;

		public SectionsPagerAdapter(FragmentManager fm, ArrayList<MarkovResults> list) {
			super(fm);
			this.list = list;
		}

		@Override
		public NameGeneratorListFragment getItem(int position) {
			NameGeneratorListFragment fragment = new NameGeneratorListFragment();
			Bundle bundle = new Bundle();
			if (isInfoTab(position)) {
				bundle.putBoolean(NameGeneratorListFragment.ARGUMENT_IS_INFO_FRAGMENT, true);
			} else {
				try {
					bundle.putBoolean(NameGeneratorListFragment.ARGUMENT_IS_INFO_FRAGMENT, false);
					bundle.putString(NameGeneratorListFragment.ARGUMENT_FILENAME, list.get(position).getCacheFile().getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(LOGTAG, "Can't provide the chace location for the NameGeneratorFragment at position " + position, e);
				}
			}
			fragment.setArguments(bundle);
			return fragment;
		}

		@Override
		public int getItemPosition(Object object) {
			//return super.getItemPosition(object);
			return POSITION_NONE;
		}

		@Override
		public int getCount() {
			int count = list.size();
			if (isShowInfoTab()) {
				count++;
			}
			return count;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if (isInfoTab(position)) {
				return getString(R.string.more_categories);
			} else
				return list.get(position).getName();
		}

		private boolean isInfoTab(int position) {
			return isShowInfoTab() && position == list.size();
		}
	}

	private class MarkovTrainingTask extends AsyncTask<MarkovTrainingTaskElement, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(MarkovTrainingTaskElement... params) {
			for (MarkovTrainingTaskElement element : params) {
				if (isCancelled()) return false;

				MarkovTraining training = element.getTraining();
				File file = element.getSourceFile();

				String pc_names_file = getString(R.string.markov_tab_all_player_characters) + NAME_GENERATOR_SOURCE_EXTENSION;
				if (file.getName().equals(pc_names_file)) continue;

				try {
					trainMarkov(training, file);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(LOGTAG, "Failed to train markov from this source file: " + file.getAbsolutePath(), e);
					setProgressBarVisibille(false);
				}
			}
			return true;
		}

		@Override
		protected void onPreExecute() {
			//if (getProgressBar() != null) {
			//	getProgressBar().setVisibility(View.VISIBLE);
			//}
			setProgressBarVisibille(true);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			//if (getProgressBar() != null) {
			//	getProgressBar().setVisibility(View.GONE);
			//}
			setProgressBarVisibille(false);
			updateViewPager();
		}

		private void trainMarkov(MarkovTraining training, File sourceFile) throws IOException {
			FileInputStream inputStream = new FileInputStream(sourceFile.getPath());
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				training.add(strLine);
			}
			br.close();
		}
	}

	private class MarkovTrainingTaskElement {
		private MarkovTraining training;
		private File sourceFile;

		public MarkovTrainingTaskElement(MarkovTraining training, File sourceFile) {
			this.training = training;
			this.sourceFile = sourceFile;
		}

		public MarkovTraining getTraining() {
			return training;
		}

		public File getSourceFile() {
			return sourceFile;
		}
	}
}