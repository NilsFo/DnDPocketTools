package de.wavegate.tos.dndpockettools.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import de.wavegate.tos.dndpockettools.MainActivity;
import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.util.Vignere;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 07.04.2016.
 */
public class Campaign implements Comparable<Campaign> {

	public static final char CHARACTER_VALUE_SEPERATOR = '#';
	public static final char CHARACTER_SEPERATOR = '+';
	public static final char CAPAIGN_VALUE_SEPERATOR = '%';

	public static final String TAG = MainActivity.PREFERENCES_TAG + "campaign_";
	public static final String TAG_GM_NAME = "gm_";
	public static final String TAG_NAME = "name_";
	public static final String TAG_PROPERTIES_CHARACTER_COUNT = "character_count";
	public static final String TAG_CAMPAIGN_NAMES = TAG + "names_list";

	private String name;
	private String GMName;

	public Campaign(String name) {
		this.name = name;
	}

	public static ArrayList<Campaign> loadAll(Context context) {
		ArrayList<Campaign> list = new ArrayList<>();
		for (String s : getAllCampaignNames(context)) {
			Campaign c = new Campaign(s);
			c.load(context);
			list.add(c);
		}
		Collections.sort(list);
		Log.i(LOGTAG, "List of all campaigns: " + list);

		return list;
	}

	public static ArrayList<String> getAllCampaignNamesAsArray(Context context) {
		Set<String> set = getAllCampaignNames(context);

		ArrayList<String> list = new ArrayList<>();
		if (!(set == null)) {
			for (String s : set) list.add(s);
			Collections.sort(list);
		}
		return list;
	}

	public static Set<String> getAllCampaignNames(Context context) {
		Set<String> set = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(TAG_CAMPAIGN_NAMES, null);
		if (set == null) {
			return new HashSet<String>();
		} else return set;
	}

	public static Set<Character> getInvalidChars() {
		Set<Character> set = new HashSet<>();

		set.add(CAPAIGN_VALUE_SEPERATOR);
		set.add(CHARACTER_VALUE_SEPERATOR);
		set.add(CHARACTER_SEPERATOR);

		return set;
	}

	public String getGMName() {
		return GMName;
	}

	public void setGMName(String GMName) {
		this.GMName = GMName;
	}

	public void addCharacter(Context context, PlayerCharacter character) {
		character.save(context, this);
	}

	public void deleteCharacter(Context context, PlayerCharacter character) {
		character.delete(context, this);
	}

	public void moveCharacter(Context context, PlayerCharacter character, Campaign targetCampaign) {
		deleteCharacter(context, character);
		targetCampaign.addCharacter(context, character);
	}

	public void removeAllCharacters(Context context) {
		ArrayList<PlayerCharacter> characters = getAll(context);
		for (PlayerCharacter c : characters) {
			deleteCharacter(context, c);
		}
	}

	public String rename(Context context, String newName) {
		String oldname = getName();
		Campaign temp = new Campaign(newName);

		ArrayList<PlayerCharacter> characters = getAll(context);
		for (PlayerCharacter c : characters) {
			moveCharacter(context, c, temp);
		}

		delete(context);
		//name = temp.getName();
		setName(temp.getName());
		save(context);
		return oldname;
	}

	public ArrayList<PlayerCharacter> getAll(Context context) {
		ArrayList<PlayerCharacter> list = new ArrayList<>();
		SharedPreferences preferences = getPreferences(context);
		int maxID = preferences.getInt(PlayerCharacter.NEXT_ID_TAG, 0);
		if (maxID == 0) return list;

		for (int i = 0; i < maxID; i++) {
			String characterTag = PlayerCharacter.getPreferenceTag(this, i) + PlayerCharacter.TAG_NAME;
			//Log.i(LOGTAG, "Trying to find a character at: " + characterTag);
			if (preferences.contains(characterTag)) {
				PlayerCharacter c = PlayerCharacter.load(context, this, i);
				list.add(c);
			}
		}

		return list;
	}

	public void clearPlayers(Context context) {
		ArrayList<PlayerCharacter> characters = getAll(context);
		for (PlayerCharacter character : characters) {
			character.delete(context, this);
		}
	}

	public void delete(Context context) {
		clearPlayers(context);
		SharedPreferences.Editor editor = getPreferences(context).edit();
		Log.i(LOGTAG, "Attempting to delete campaign '" + getName() + "'. Currently registered campaigns: " + getPreferences(context).getStringSet(TAG_CAMPAIGN_NAMES, null));
		Set<String> names = getAllCampaignNames(context);
		names.remove(getName());
		editor.putStringSet(TAG_CAMPAIGN_NAMES, names);
		editor.apply();
		Log.i(LOGTAG, "After delete attempt registered campaigns: " + getPreferences(context).getStringSet(TAG_CAMPAIGN_NAMES, null) + " getAll(): " + Campaign.getAllCampaignNames(context));
	}

	public boolean load(Context context) {
		SharedPreferences preferences = getPreferences(context);

		if (exists(context)) {
			Log.i(LOGTAG, "Loading successed.");
			setGMName(preferences.getString(getPreferenceTag() + TAG_GM_NAME, context.getString(R.string.error_unknown)));
			return true;
		} else {
			Log.i(LOGTAG, "Loading failed. Tag does not exist.");
			return false;
		}
	}

	public boolean exists(Context context) {
		return getAllCampaignNames(context).contains(getName());
	}

	public void save(Context context) {
		Set<String> names = getAllCampaignNames(context);
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putString(getPreferenceTag() + TAG_GM_NAME, getGMName());
		if (!names.contains(getName())) {
			names.add(getName());
			editor.putStringSet(TAG_CAMPAIGN_NAMES, names);
		}

		editor.apply();
	}

	public void makeNamesValid(Context context) {
		String newName = getName();
		String newGMName = getGMName();
		for (Character c : getInvalidChars()) {
			String s = String.valueOf(c);
			newName = newName.replace(s, "");
			newGMName = newName.replace(s, "");

			for (PlayerCharacter character : getAll(context)) {
				if (character.hasValidName()) continue;

				character.setName(character.getName().replace(s, ""));
				character.setPlayerName(character.getPlayerName().replace(s, ""));
				character.save(context, this);
			}
		}

		if (!hasValidName()) {
			setGMName(newGMName);
			rename(context, newName);
		}
		save(context);
	}

	public boolean hasValidNames(Context context) {
		if (!hasValidName()) return false;

		for (PlayerCharacter character : getAll(context)) {
			if (!character.hasValidName()) return false;
		}
		return true;
	}

	public boolean hasValidName() {
		for (Character c : getInvalidChars()) {
			String s = getName() + getGMName();
			if (s.contains(String.valueOf(c))) {
				return false;
			}
		}

		return true;
	}

	public String toQRString(Context context) {
		String code = getName() + CAPAIGN_VALUE_SEPERATOR;
		code = code + getGMName() + CAPAIGN_VALUE_SEPERATOR;

		if (hasPlayerCharacters(context)) {
			for (PlayerCharacter c : getAll(context)) {
				code = code + CHARACTER_SEPERATOR;
				code = code + c.toQRString();
				code = code + CHARACTER_SEPERATOR;
			}
		}

		Log.i(LOGTAG, toString() + " -- QR --> " + code);
		code = new Vignere().vignereCrypt(code, TAG, true);
		Log.i(LOGTAG, "Encoded QR code: " + code);

		return code;
	}

	public Properties toProperties(Context context) {
		Properties properties = new Properties();
		ArrayList<PlayerCharacter> characters = getAll(context);

		properties.put(TAG_NAME, getName());
		properties.put(TAG_GM_NAME, getName());
		properties.put(TAG_PROPERTIES_CHARACTER_COUNT, String.valueOf(characters.size()));
		int i = 0;
		for (PlayerCharacter character : characters) {
			properties.putAll(character.toMap(i++));
		}

		return properties;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getPreferenceTag() {
		String tag = TAG + getName();
		if (!tag.endsWith("_")) tag += "_";
		return tag;
	}

	public int countPlayerCharacters(Context context) {
		return getAll(context).size();
	}

	public boolean hasPlayerCharacters(Context context) {
		return countPlayerCharacters(context) != 0;
	}

	@Override
	public String toString() {
		return "Campaign '" + getName() + "', by " + getGMName();
	}

	private SharedPreferences getPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
		//	return context.getSharedPreferences(
		//			context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
	}

	@Override
	public int compareTo(Campaign another) {
		return getName().compareTo(another.getName());
	}
}
