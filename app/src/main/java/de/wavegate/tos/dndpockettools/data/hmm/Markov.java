package de.wavegate.tos.dndpockettools.data.hmm;

import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Nils on 24.05.2016.
 */
public class Markov {

	public static final String START_SYMBOL = "%";
	public static final String END_SYMBOL = "#";

	public static final String LOGTAG = "Markov";

	private int averageLength;
	private int maxLength;
	private int lookahead;

	private HashMap<String, Vector<Character>> probabilityMap;
	private Vector<Character> firstChars, lastChars;

	public Markov(Collection<String> resource, int lookahead) {
		this.lookahead = lookahead;

		probabilityMap = new HashMap<>();
		firstChars = new Vector<>();
		lastChars = new Vector<>();

		train(resource);

		setMaxLength(getAverageLength() * 3);
	}

	private void train(Collection<String> resource) {
		long allLength = 0;
		int look = hasLookahead() ? getLookahead() : 1;

		for (String s : resource) {
			if (!hasLookahead()) {
				char first = s.charAt(0);
				char last = s.charAt(s.length() - 1);

				firstChars.add(first);
				lastChars.add(last);
			} else {
				s = s.trim().replace(START_SYMBOL, "").replace(END_SYMBOL, "");
				s = START_SYMBOL + s + END_SYMBOL;
				for (int i = 0; i < look; i++) {
					if (i + 1 >= s.length()) {
						continue;
					}
					//putProbabillity(START_SYMBOL + s.substring(0, i), s.charAt(i + 1));
				}
			}

			for (int i = 0; i < s.length(); i++) {
				for (int lookahead = 0; lookahead < look; lookahead++) {
					//boolean outOfBounds = false;
					//for (int j=0;j<lookahead;j++){
					//    boolean b = i+j+1 >s.length();
					//    outOfBounds |= b;
					//    if (hasLookahead()&&b){
					//
					//    }
					//
					//}
					//if (!hasLookahead()&&outOfBounds)continue;
					if (i + lookahead + 1 >= s.length()) continue;

					String current = String.valueOf(s.charAt(i));
					if (hasLookahead())
						current = String.valueOf(s.substring(i, i + lookahead + 1));
					//Log.i(LOGTAG, "Training from: '" + s + "', position: " + i + " lookahead: " + lookahead + " -> " + current);

					putProbabillity(current, s.charAt(i + lookahead + 1));
					//if (!probabilityMap.containsKey(current))
					//	probabilityMap.put(current, new Vector<Character>());
					//probabilityMap.get(current).add(s.charAt(i + 1));
				}
			}
			allLength += s.length();
		}

		probabilityMap.remove("");

		averageLength = 0;
		if (resource.size() != 0)
			averageLength = (int) (allLength / (long) resource.size());
		if (averageLength < 1) averageLength = 1;

		Log.i(LOGTAG, "Markov training complete. Avg size: " + averageLength);
		Log.v(LOGTAG, "Markov Transitions: " + probabilityMap);
	}

	private void putProbabillity(String key, Character value) {
		if (!probabilityMap.containsKey(key))
			probabilityMap.put(key, new Vector<Character>());
		probabilityMap.get(key).add(value);
	}

	public int getLookahead() {
		return lookahead;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public String nextString() {
		return nextString(new Random());
	}

	public String nextString(Random random) {
		if (!hasLookahead())
			return nextStringNoLookahead(random);

		return nextString(getLookahead(), random);
	}

	public boolean hasLookahead() {
		return getLookahead() > 1;
	}

	public String nextString(int lookahead, Random random) {
		if (!probabilityMap.containsKey(START_SYMBOL)) return "";

		String s = START_SYMBOL;
		for (int i = 1; i < getMaxLength(); i++) {
			boolean roundCompleted = false;
			Log.i(LOGTAG, "Markov creation. =========================================");
			for (int l = lookahead; l > 0; l--) {
				Log.i(LOGTAG, "Markov creation. So far created: '" + s + "', current lookback = " + l);
				if (i - l < 0) continue;
				if (roundCompleted) break;

				String current;
				current = s.substring(i - l, i);

				Log.i(LOGTAG, "Markov creation. Current chunk to look at: " + current);
				char next = '?';
				if (probabilityMap.containsKey(current)) {
					Vector<Character> v = probabilityMap.get(current);
					next = v.elementAt(random.nextInt(v.size()));
					roundCompleted = true;
				} else {
					if (i == 0) {
						next = START_SYMBOL.charAt(0);
						roundCompleted = true;
					}
				}

				if (roundCompleted)
					s = s + next;
			}

			if (s.endsWith(END_SYMBOL)) break;
		}

		Log.i(LOGTAG, "Result: " + s);

		return s.replace(START_SYMBOL, "").replace(END_SYMBOL, "");
	}

	private String nextStringNoLookahead(Random random) {
		if (firstChars.isEmpty()) return "";

		String s = "";
		s = s + firstChars.get(random.nextInt(firstChars.size()));
		boolean endBecauseOfLength = false;

		for (int i = 0; i < getMaxLength(); i++) {
			endBecauseOfLength |= endBecauseOfLength(random, i);
			char c = s.charAt(i);
			String current = String.valueOf(c);

			if (endBecauseOfLength && lastChars.contains(c))
				break;

			char next = '?';
			if (probabilityMap.containsKey(current)) {
				Vector<Character> v = probabilityMap.get(current);
				next = v.elementAt(random.nextInt(v.size()));
			} else {
				next = firstChars.elementAt(random.nextInt(firstChars.size()));
			}
			s = s + next;
		}
		return s;
	}

	private boolean endBecauseOfLength(Random random, int position) {
		if (position < averageLength / 2) return false;

		double percentage = ((double) position / (double) (averageLength * 1.5));
		int chance = (int) (percentage * 100 / 2);
		//Log.i(MainActivity.LOGTAG, "Running Markov... Round: " + position + " End Change: " + chance + "%");
		return random.nextInt(100) <= chance;
	}

	public int getAverageLength() {
		return averageLength;
	}

}