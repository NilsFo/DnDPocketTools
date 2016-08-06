package de.wavegate.tos.dndpockettools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import de.wavegate.tos.dndpockettools.activity.SettingsActivity;
import de.wavegate.tos.dndpockettools.activity.mainfragments.AoEDisplayerFragment;
import de.wavegate.tos.dndpockettools.activity.mainfragments.MainMenuFragment;
import de.wavegate.tos.dndpockettools.activity.mainfragments.NameGeneratorFragment;
import de.wavegate.tos.dndpockettools.activity.mainfragments.PartyStatsFragment;
import de.wavegate.tos.dndpockettools.activity.mainfragments.RollDistributionsFragment;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, MainMenuFragment.OnFragmentInteractionListener, DrawerLayout.DrawerListener {

	public static final String PREFERENCES_TAG = "de.wavegate.tos.dnd_pocket_tools_";
	public static final String ACTIVE_FRAME_CLASS_NAME = "active_fragem_class_name";
	public static String LOGTAG = "NilsD&DUtils";
	public static String LOGTAG_VERBOUSE = "NilsD&DUtils Closeup";
	private MainMenuFragment activeFragment;
	private FloatingActionButton fab;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(LOGTAG, "============== Main: onCreate() - " + android.os.Build.MODEL + " ==============");
		Log.i(LOGTAG_VERBOUSE, "Main: onCreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		setSupportActionBar(toolbar);

		progressBar = (ProgressBar) findViewById(R.id.mainMenu_progressBar);

		fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.i(LOGTAG, "MainActivity: FAB clicked.");
				if (activeFragment != null) {
					Log.i(LOGTAG, "The current active fragment exists and will be informed about the FAB click event.");
					activeFragment.onFABClicked();
				}
			}
		});

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		//drawer.setDrawerListener(toggle);
		if (drawer != null) {
			drawer.addDrawerListener(toggle);
			drawer.addDrawerListener(this);
		}
		toggle.syncState();

		if (navigationView != null) {
			navigationView.setNavigationItemSelectedListener(this);
			navigationView.getMenu().getItem(0).setChecked(true);
		}
//		drawer.setScrimColor(getResources().getColor(android.R.color.transparent));

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		Log.i(LOGTAG_VERBOUSE, "From last instance: " + savedInstanceState);
		if (savedInstanceState == null) {
			requestFragmentTransition(PartyStatsFragment.class, new Bundle());
		} else {
			Log.i(LOGTAG, "deep inside i remember something... " + savedInstanceState.getString(ACTIVE_FRAME_CLASS_NAME));
		}
	}

	public boolean requestFragmentTransition(Class fragmentSource, Bundle arguments) {
		Object o;
		try {
			o = fragmentSource.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(LOGTAG, "Wanted to change fragment, but it failed! " + e.getMessage(), e);
			return false;
		}

		if (o instanceof MainMenuFragment) {
			MainMenuFragment f = (MainMenuFragment) o;
			f.setArguments(arguments);
			transitionFragment(f);
			return true;
		} else {
			Log.e(LOGTAG, "Wanted to change fragment, but " + o + " is no legit MainMenu-Fragment!");
			return false;
		}
	}

	private void transitionFragment(MainMenuFragment fragment) {
		Log.i(LOGTAG, "Fragment Transition request recieved! New: " + fragment + " Currently: " + activeFragment);
		Log.i(LOGTAG_VERBOUSE, "Fragment Transition request recieved! New: " + fragment + " Currently: " + activeFragment);
		if (fragment == null) return;

		if (activeFragment == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
		} else {
			if (activeFragment.getClass().getCanonicalName().equals(fragment.getClass().getCanonicalName())) {
				return;
			}

			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
			transaction.replace(R.id.fragment_container, fragment);
			transaction.commit();
		}

		activeFragment = fragment;
		onFragmentChange(fragment);
	}

	public void onFragmentChange(MainMenuFragment fragment) {
		activeFragment = fragment;
		Log.i(LOGTAG_VERBOUSE, "Registered a FragmentChange. New Fragment is a " + fragment.getClass().getName());
		Log.i(LOGTAG, "Registered a FragmentChange. New Fragment is a " + fragment.getClass().getName());

		invalidateFragmentUI();
		setProgressBarVisible(false);
	}

	public void setProgressBarVisible(Boolean visibility) {
		int vis;
		if (visibility) vis = View.VISIBLE;
		else vis = View.GONE;
		if (progressBar != null) progressBar.setVisibility(vis);
	}

	public void invalidateFragmentUI() {
		if (activeFragment == null) {
			return;
		}

		if (activeFragment.getFABVisible()) {
			fab.setImageResource(activeFragment.getFABIcon());
			fab.show();
		} else
			fab.hide();

		invalidateOptionsMenu();
		getSupportActionBar().setTitle(activeFragment.getTitle());
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
			return;
		}

		if (activeFragment != null && !activeFragment.onBackPressed()) {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(LOGTAG, "Main activity told to change Options menu!");
		// Inflate the menu; this adds items to the action bar if it is present.
		int id = R.menu.empty;
		if (activeFragment != null) {
			id = activeFragment.getMenu();
			Log.i(LOGTAG, "I got got an active fragment. Will inflate that. Fragment coming from: " + activeFragment.getClass().getName());
		} else {
			Log.i(LOGTAG, "No active fragment found.");
		}
		menu.clear();
		getMenuInflater().inflate(id, menu);

		return true;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.i(LOGTAG, "They told me to prepare menu: " + menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(LOGTAG, "Main menu got a menu interaction! " + item.getTitleCondensed());

		if (activeFragment != null) {
			return activeFragment.onOptionsItemSelected(item);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		Log.i(LOGTAG, "Navigation Item was selected: " + item.getTitleCondensed());
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_rollDistribution) {
			requestFragmentTransition(RollDistributionsFragment.class, new Bundle());
		} else if (id == R.id.nav_my_parties) {
			requestFragmentTransition(PartyStatsFragment.class, new Bundle());
		} else if (id == R.id.nav_manage) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		} else if (id == R.id.nav_aoe_displayer) {
			requestFragmentTransition(AoEDisplayerFragment.class, new Bundle());
		} else if (id == R.id.nav_name_generator) {
			requestFragmentTransition(NameGeneratorFragment.class, new Bundle());
		} else if (id == R.id.battleReferences) {
			String url = "https://crobi.github.io/dnd5e-quickref/preview/quickref.html";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer != null) {
			drawer.closeDrawer(GravityCompat.START);
		}
		return true;
	}

	@Override
	public void onFragmentInteraction(MainMenuFragment source, Uri uri) {
		Log.i(LOGTAG, "Fragment wants to interact with me: " + source + " - " + uri);
	}

	@Override
	public void onDrawerSlide(View drawerView, float slideOffset) {
	}

	@Override
	public void onDrawerOpened(View drawerView) {
	}

	@Override
	public void onDrawerClosed(View drawerView) {
	}

	@Override
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		Log.i(LOGTAG, "MainActivity - SavingInstance now.");
		super.onSaveInstanceState(outState, outPersistentState);
		if (activeFragment != null) {
			outState.putString(activeFragment.getClass().getCanonicalName(), ACTIVE_FRAME_CLASS_NAME);
		}
	}

	///@Override
	///public void onDrawerOpened(View drawerView) {
	///	Log.i(LOGTAG, "Drawer opened.");
	///	getSupportActionBar().setTitle(R.string.app_name);
	///}
///
	///@Override
	///public void onDrawerClosed(View drawerView) {
	///	Log.i(LOGTAG, "Drawer opened.");
	///	if (getSupportActionBar() != null && !(activeFragment == null)) {
	///		Log.i(LOGTAG, "Able to change the AppBar title: " + getResources().getString(activeFragment.getTitle()));
	///		getSupportActionBar().setTitle(activeFragment.getTitle());
	///	} else {
	///		Log.w(LOGTAG, "Wanted to change the App's title, but it failed!");
	///	}
	///}

	//public void setAppBarTitle(String title) {
	//	//Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
	//	//setSupportActionBar(mActionBarToolbar);
	//	//if (title == null) title = "";
	//	getSupportActionBar().setTitle(title);
	//}

	@Override
	public void onDrawerStateChanged(int newState) {

	}
}
