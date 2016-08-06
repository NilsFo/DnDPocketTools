package de.wavegate.tos.dndpockettools.activity.mainfragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.widget.ProgressBar;

import de.wavegate.tos.dndpockettools.MainActivity;
import de.wavegate.tos.dndpockettools.R;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 02.04.2016.
 */
public abstract class MainMenuFragment extends Fragment {

	protected OnFragmentInteractionListener mListener;

	public MainMenuFragment() {

	}

	public final void informMainActivity(Context context) {
		if (context instanceof MainActivity) {
			MainActivity activity = (MainActivity) context;
			activity.onFragmentChange(this);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	protected SharedPreferences getPreferences() {
		if (getContext() == null) {
			Log.e(LOGTAG, "ERROR! Context is null!");
		}
		return PreferenceManager.getDefaultSharedPreferences(getContext());
	}

	public ProgressBar getProgressBar() {
		MainActivity activity = getMainActivity();
		if (activity != null) {
			return getMainActivity().getProgressBar();
		}
		return null;
	}

	public void setProgressBarVisibille(Boolean visible) {
		MainActivity activity = getMainActivity();
		if (activity != null) {
			activity.setProgressBarVisibility(visible);
		}
	}

	public boolean getFABVisible() {
		return false;
	}

	public void onFABClicked() {
		Log.i(LOGTAG, "The FAB was clicked. But no one came.");
	}

	@Nullable
	protected final MainActivity getMainActivity() {
		Activity activity = getActivity();
		if (activity != null && activity instanceof MainActivity) return (MainActivity) activity;
		return null;
	}

	public int getFABIcon() {
		return android.R.drawable.ic_dialog_info;
	}

	public int getTitle() {
		return R.string.app_name;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		Log.i(LOGTAG, "They told me (Fragment) to prepare menu: " + menu);
		super.onPrepareOptionsMenu(menu);
	}

	public int getMenu() {
		return R.menu.empty;
	}

	public boolean onBackPressed() {
		return false;
	}


	//	public boolean onOptionsItemSelected(MenuItem item){
//		return true;
//	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(MainMenuFragment source, Uri uri);
	}
}
