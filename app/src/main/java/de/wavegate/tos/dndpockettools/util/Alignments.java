package de.wavegate.tos.dndpockettools.util;

import android.content.Context;

import java.util.ArrayList;

import de.wavegate.tos.dndpockettools.R;

/**
 * Created by Nils on 08.04.2016.
 */
public class Alignments {

	private static Alignments alignments;
	private ArrayList<String> alignmentStrings, alignmentStringsShort;

	private Alignments(Context context) {
		alignmentStrings = new ArrayList<>();
		alignmentStringsShort = new ArrayList<>();

		alignmentStrings.add(context.getString(R.string.alignment_none));
		alignmentStrings.add(context.getString(R.string.alignment_lg));
		alignmentStrings.add(context.getString(R.string.alignment_ng));
		alignmentStrings.add(context.getString(R.string.alignment_cg));
		alignmentStrings.add(context.getString(R.string.alignment_ln));
		alignmentStrings.add(context.getString(R.string.alignment_n));
		alignmentStrings.add(context.getString(R.string.alignment_cn));
		alignmentStrings.add(context.getString(R.string.alignment_le));
		alignmentStrings.add(context.getString(R.string.alignment_ne));
		alignmentStrings.add(context.getString(R.string.alignment_ce));

		alignmentStringsShort.add(context.getString(R.string.alignment_none_s));
		alignmentStringsShort.add(context.getString(R.string.alignment_lg_s));
		alignmentStringsShort.add(context.getString(R.string.alignment_ng_s));
		alignmentStringsShort.add(context.getString(R.string.alignment_cg_s));
		alignmentStringsShort.add(context.getString(R.string.alignment_ln_s));
		alignmentStringsShort.add(context.getString(R.string.alignment_n_s));
		alignmentStringsShort.add(context.getString(R.string.alignment_cn_s));
		alignmentStringsShort.add(context.getString(R.string.alignment_le_s));
		alignmentStringsShort.add(context.getString(R.string.alignment_ne_s));
		alignmentStringsShort.add(context.getString(R.string.alignment_ce_s));
	}

	public static Alignments get(Context context) {
		if (alignments == null)
			alignments = new Alignments(context);
		return alignments;
	}

	public ArrayList<String> getLong() {
		return new ArrayList<>(alignmentStrings);
	}

	public String getShort(int id) {
		if (id >= alignmentStringsShort.size()) return "?";
		return alignmentStringsShort.get(id);
	}

	public String getLong(int id) {
		if (id >= alignmentStrings.size()) return "?";
		return alignmentStrings.get(id);
	}

	public String get(int id, boolean shortVersion) {
		if (shortVersion) return getShort(id);
		return getLong(id);
	}

}
