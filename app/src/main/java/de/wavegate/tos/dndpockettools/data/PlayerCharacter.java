package de.wavegate.tos.dndpockettools.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.wavegate.tos.dndpockettools.MainActivity;
import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.util.Alignments;

/**
 * Created by Nils on 06.04.2016.
 */
public class PlayerCharacter extends Creature {

	public static final String NEXT_ID_TAG = MainActivity.PREFERENCES_TAG + "next_character_id";
	public static final String TAG = "pc_";
	public static final int ID_NOT_SET_YET = -1;

	public static final String TAG_XP = "_xp";
	public static final String TAG_PLAYER_NAME = "_player_name";
	public static final String TAG_NOTES = "_notes";
	public static final String TAG_NAME = "_name";
	public static final String TAG_ALIGNMENT = "_alignment";
	public static final String TAG_AC = "_armor_class";

	private static ArrayList<Integer> xpPerLevel;

	private int XP;
	private int ID;
	private int alignment;
	private String playerName, notes;

	private PlayerCharacter() {
		this("", 0, 0, 0, "", 0);
	}

	public PlayerCharacter(String name, int initiative, int AC, int XP, String playerName, int alignment) {
		super(name, initiative, AC);
		this.XP = XP;
		this.playerName = playerName;
		notes = "";
		this.alignment = alignment;
		this.ID = ID_NOT_SET_YET;
	}

	private static synchronized void setupLevelList() {
		if (xpPerLevel != null) {
			return;
		}

		xpPerLevel = new ArrayList<>();
		xpPerLevel.add(0);
		xpPerLevel.add(300);
		xpPerLevel.add(900);
		xpPerLevel.add(2700);
		xpPerLevel.add(6500);
		xpPerLevel.add(14000);
		xpPerLevel.add(23000);
		xpPerLevel.add(34000);
		xpPerLevel.add(48000);
		xpPerLevel.add(64000);
		xpPerLevel.add(85000);
		xpPerLevel.add(100000);
		xpPerLevel.add(120000);
		xpPerLevel.add(140000);
		xpPerLevel.add(165000);
		xpPerLevel.add(195000);
		xpPerLevel.add(255000);
		xpPerLevel.add(265000);
		xpPerLevel.add(305000);
		xpPerLevel.add(355000);
	}

	private static SharedPreferences getPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static PlayerCharacter load(Context context, Campaign campaign, int ID) {
		SharedPreferences preferences = getPreferences(context);
		String tag = getPreferenceTag(campaign, ID);
		PlayerCharacter character = new PlayerCharacter();

		character.setXP(preferences.getInt(tag + TAG_XP, 0));
		character.setAC(preferences.getInt(tag + TAG_AC, 0));
		character.setName(preferences.getString(tag + TAG_NAME, context.getString(R.string.error_unknown)));
		character.setPlayerName(preferences.getString(tag + TAG_PLAYER_NAME, context.getString(R.string.error_unknown)));
		character.setNotes(preferences.getString(tag + TAG_NOTES, context.getString(R.string.error_unknown)));
		character.setAlignment(preferences.getInt(tag + TAG_ALIGNMENT, 0));
		character.ID = ID;

		return character;
	}

	public static String getPreferenceTag(Campaign campaign, int ID) {
		return campaign.getPreferenceTag() + TAG + ID;
	}

	public static int getLevel(int currentXP) {
		setupLevelList();
		int level = 0;
		for (int xp : xpPerLevel) {
			if (currentXP >= xp) level++;
		}
		return level;
	}

	public int getAlignment() {
		return alignment;
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	public Map<String, Object> toMap(int identifier) {
		Map<String, Object> map = new HashMap<>();
		if (getID() == ID_NOT_SET_YET) return map;

		String tag = getToMapBaseTag(identifier);
		map.put(tag + TAG_AC, String.valueOf(getAC()));
		map.put(tag + TAG_NAME, getName().replace("\n", ""));
		map.put(tag + TAG_NOTES, getNotes());
		map.put(tag + TAG_PLAYER_NAME, getPlayerName().replace("\n", ""));
		map.put(tag + TAG_XP, String.valueOf(getXP()));
		map.put(tag + TAG_ALIGNMENT, String.valueOf(getAlignment()));

		return map;
	}

	public static String getToMapBaseTag(int identifier){
		return  TAG + identifier;
	}

	public void save(Context context, Campaign campaign) {
		SharedPreferences preferences = getPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();

		if (getID() == ID_NOT_SET_YET) {
			int nextID = preferences.getInt(NEXT_ID_TAG, 0);
			ID = nextID;
			editor.putInt(NEXT_ID_TAG, nextID + 1);
		}

		String tag = getPreferenceTag(campaign, ID);
		editor.putInt(tag + TAG_AC, getXP());
		editor.putString(tag + TAG_NAME, getName());
		editor.putString(tag + TAG_NOTES, getNotes());
		editor.putString(tag + TAG_PLAYER_NAME, getPlayerName());
		editor.putInt(tag + TAG_XP, getXP());
		editor.putInt(tag + TAG_ALIGNMENT, getAlignment());

		editor.apply();
	}

	public void delete(Context context, Campaign campaign) {
		if (getID() == ID_NOT_SET_YET) return;

		String tag = getPreferenceTag(campaign, ID);
		SharedPreferences preferences = getPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();

		editor.remove(tag + TAG_AC);
		editor.remove(tag + TAG_NAME);
		editor.remove(tag + TAG_NOTES);
		editor.remove(tag + TAG_PLAYER_NAME);
		editor.remove(tag + TAG_XP);
		editor.remove(tag + TAG_ALIGNMENT);

		editor.apply();
	}

	public boolean hasValidName() {
		for (Character c : Campaign.getInvalidChars()) {
			String s = getName() + getPlayerName();
			if (s.contains(String.valueOf(c))) {
				return false;
			}
		}

		return true;
	}

	public int modXP(int xp) {
		setXP(getXP() + xp);
		return getXP();
	}

	public int getXP() {
		return XP;
	}

	public void setXP(int XP) {
		this.XP = XP;
	}

	public String getAlignment(Context context, boolean shortForm) {
		return Alignments.get(context).get(getAlignment(), shortForm);
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getNextLevelXP() {
		int level = getLevel();
		return xpPerLevel.get(level) - getXP();
	}

	public int getID() {
		return ID;
	}

	public String toQRString() {
		//String name, int initiative, int AC, int XP, String playerName, int alignment
		char c = Campaign.CHARACTER_VALUE_SEPERATOR;
		String code = c + getName() + c;
		code = code + getXP() + c;
		code = code + getPlayerName() + c;
		code = code + getAlignment() + c;

		Log.v(MainActivity.LOGTAG, toString() + " -- QR --> " + code);
		return code;
	}

	@Override
	public String toString() {
		return "PlayerCharacter [" + getID() + "] '" + getName() + "' by " + getPlayerName() + " XP: " + getXP();
	}

	public int getLevel() {
		setupLevelList();
		return getLevel(getXP());
	}
}
