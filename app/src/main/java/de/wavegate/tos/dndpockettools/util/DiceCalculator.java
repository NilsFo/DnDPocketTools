package de.wavegate.tos.dndpockettools.util;

import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Nils on 31.03.2016.
 */
public class DiceCalculator {

	private int r, n;
	private double rn;
	private HashMap<Integer, Double> probabilityMap;

	/**
	 * Sets up a new DiceCalculator.
	 *
	 * @param r The dice Type (Max value on a dice)
	 * @param n The number of Dice
	 */
	public DiceCalculator(int n, int r) {
		this.n = n;
		this.r = r;
		rn = Math.pow(r, n);

		probabilityMap = new HashMap<>();
	}

	public synchronized double getProbability(int k) {
		if (!isRollable(k)) return 0;

		if (probabilityMap.containsKey(k)) {
			return probabilityMap.get(k);
		}

		long p = 0;
		for (int i = 0; i < ((k - n) / r) + 1; i++) {
			p += Math.pow(-1, i) * CombinatoricsUtils.binomialCoefficient(n, i) * CombinatoricsUtils.binomialCoefficient(k - r * i - 1, n - 1);
		}

		double ret = (double) p / rn;
		probabilityMap.put(k, ret);
		return ret;
	}

	public synchronized void rollAllProbabilities() {
		rollAllProbabilities(getDiceValue() * 10000);
	}

	public synchronized void rollAllProbabilities(int repeats) {
		rollAllProbabilities(repeats, new Random());
	}

	public synchronized void rollAllProbabilities(int repeats, Random rng) {
		probabilityMap.clear();
		int[] rolls = new int[getMaxValue() - getMinValue() + 1];
		double allRolls = 0;

		for (int i = 0; i < repeats; i++) {
			int roll = getSample(rng);
			rolls[roll - getMinValue()]++;
		}
		for (int roll : rolls)
			allRolls += roll;

		for (int i = 0; i < rolls.length; i++) {
			//distribution[i] = ((double) rolls[i] / allRolls) * 100;
			probabilityMap.put(i + getMinValue(), ((double) rolls[i] / allRolls));
		}
	}

	public synchronized void calculateAllProbabilities() {
		for (int i = getMinValue(); i < getMaxValue() + 1; i++) {
			getProbability(i);
		}
	}

	public synchronized ArrayList<Integer> getAverageRoll() {
		calculateAllProbabilities();
		ArrayList<Integer> list = new ArrayList<>();
		double currentBest = 0;
		for (int i = getMinValue(); i < getMaxValue() + 1; i++) {
			double d = probabilityMap.get(i);
			if (currentBest < d) {
				list.clear();
				currentBest = d;
			}
			if (currentBest == d) {
				list.add(i);
			}
		}
		return list;
	}

	public synchronized double getBestPercentage() {
		ArrayList<Integer> list = getAverageRoll();
		return probabilityMap.get(list.get(0));
	}

	public int getSample(Random r) {
		int res = 0;
		for (int i = 0; i < getDiceCount(); i++)
			res += r.nextInt(getDiceValue()) + 1;
		return res;
	}

	public int getSample() {
		return getSample(new Random());
	}

	public boolean isRollable(int k) {
		return k >= getMinValue() && k <= getMaxValue();
	}

	public int getMinValue() {
		return getDiceCount();
	}

	public int getMaxValue() {
		return getDiceCount() * getDiceValue();
	}

	public int getDiceCount() {
		return n;
	}

	public int getDiceValue() {
		return r;
	}

	public int getDiceSpan() {
		if (getDiceCount() == 1) return getMaxValue();
		return getMaxValue() * getDiceCount() - getMinValue();
	}

}
