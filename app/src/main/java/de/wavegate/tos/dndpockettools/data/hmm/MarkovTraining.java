package de.wavegate.tos.dndpockettools.data.hmm;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Nils on 23.05.2016.
 */
public class MarkovTraining implements Comparable<MarkovTraining> {

	public static final String IGNORE_CHAR = "#";

	private ArrayList<String> list;
	private String name;

	@Deprecated
	public MarkovTraining(ArrayList<String> list) {
		this.list = new ArrayList<>(list);
	}

	@Deprecated
	public MarkovTraining(String... list) {
		this.list = new ArrayList<String>();
		Collections.addAll(this.list, list);
	}

	public MarkovTraining(String name) {
		list = new ArrayList<>();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void forgetTraining() {
		list.clear();
	}

	public boolean add(String s) {
		if (s == null) return false;
		s = s.trim().toLowerCase();

		//Hold on to your butts!
		return s.length() != 0 && !s.startsWith(IGNORE_CHAR) && !list.contains(s) && list.add(s);
	}

	public void forceAdd(String s) {
		list.add(s);
	}

	public boolean add(String... list) {
		boolean added = false;
		for (String s : list)
			added |= add(s);
		return added;
	}

	public ArrayList<String> getAll() {
		ArrayList<String> set = new ArrayList<>();
		for (String s : list)
			set.add(s);
		return set;
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public int size() {
		return list.size();
	}

	public String getTrainingData(int index) {
		return list.get(index);
	}

	@Override
	public int compareTo(MarkovTraining another) {
		return getName().compareTo(another.getName());
	}
}
