package de.wavegate.tos.dndpockettools.data.hmm;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.activity.mainfragments.NameGeneratorFragment;
import de.wavegate.tos.dndpockettools.util.FileManager;

/**
 * Created by Nils on 08.06.2016.
 */
public class DefaultNameLists {
	private Context context;

	public DefaultNameLists(Context context) {
		this.context = context;
	}

	public ArrayList<String> createDnD5Names() {
		ArrayList<String> nameList = new ArrayList<>();
		ArrayList<String> tempList = new ArrayList<>();

		ArrayList<String> beginning = new ArrayList<>();
		ArrayList<String> middle = new ArrayList<>();
		ArrayList<String> end = new ArrayList<>();

		//beginning.add("");
		//beginning.add("");
		//beginning.add("");
		beginning.add("");
		beginning.add("a");
		beginning.add("be");
		beginning.add("de");
		beginning.add("el");
		beginning.add("fa");
		beginning.add("jo");
		beginning.add("ki");
		beginning.add("la");
		beginning.add("ma");
		beginning.add("na");
		beginning.add("o");
		beginning.add("pa");
		beginning.add("re");
		beginning.add("si");
		beginning.add("ta");
		beginning.add("va");

		middle.add("bar");
		middle.add("ched");
		middle.add("dell");
		middle.add("far");
		middle.add("gran");
		middle.add("hal");
		middle.add("jen");
		middle.add("kel");
		middle.add("lim");
		middle.add("mor");
		middle.add("net");
		middle.add("penn");
		middle.add("quil");
		middle.add("rond");
		middle.add("stark");
		middle.add("shen");
		middle.add("tur");
		middle.add("vash");
		middle.add("yor");
		middle.add("zen");

		end.add("");
		end.add("a");
		end.add("ac");
		end.add("ai");
		end.add("al");
		end.add("am");
		end.add("an");
		end.add("ar");
		end.add("ea");
		end.add("el");
		end.add("er");
		end.add("ess");
		end.add("ett");
		end.add("ic");
		end.add("id");
		end.add("il");
		end.add("in");
		end.add("is");
		end.add("or");
		end.add("us");

		for (String b : beginning) {
			for (String m : middle) {
				for (String e : end) {
					String s = b + m + e;
					tempList.add(s.trim().toLowerCase());
				}
			}
		}

		for (String s : tempList) {
			String first = s.substring(0, 1);
			String rest = s.substring(1);
			nameList.add(first.toUpperCase() + rest);
		}

		return nameList;
	}

	private boolean dnd5MarkovFileExists() {
		return getDnd5MarkovNameFile().exists();
	}

	public File getDnd5MarkovNameFile() {
		FileManager manager = new FileManager(context);
		File dir = manager.getNameGeneratorSources();

		String name = context.getString(R.string.filename_dnd5_gm_screen_names);
		return new File(dir, name + NameGeneratorFragment.NAME_GENERATOR_SOURCE_EXTENSION);
	}
}
