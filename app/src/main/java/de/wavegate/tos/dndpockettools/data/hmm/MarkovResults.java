package de.wavegate.tos.dndpockettools.data.hmm;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import de.wavegate.tos.dndpockettools.util.FileManager;

/**
 * Created by Nils on 05.06.2016.
 */
public class MarkovResults implements Comparable<MarkovResults> {

	public static final String CACHE_FILE_EXTENSION = ".txt";

	private MarkovTraining training;
	private Context context;

	public MarkovResults(MarkovTraining training, Context context) {
		this.training = training;
		this.context = context;
	}

	public String getName() {
		return training.getName();
	}

	public File getCacheFile() throws IOException {
		File cacheDir = new FileManager(context).getCacheDir();

		if (!cacheDir.exists())
			throw new IOException("Wanted to access the chace dir (" + cacheDir.getAbsolutePath() + "), but it did not exist!");
		File cacheFile = new File(cacheDir, getName() + CACHE_FILE_EXTENSION);

		if (!cacheFile.exists()) {
			//if (!cacheFile.createNewFile())
			//	throw new IOException("The cache file (" + cacheFile.getAbsolutePath() + ") did not exist. Creating the file failed as well.");
			cacheFile.createNewFile();
		}

		return cacheFile;
	}

	public ArrayList<String> getResults() throws IOException {
		ArrayList<String> list = new ArrayList<>();
		File cache = getCacheFile();
		FileInputStream stream = new FileInputStream(cache);
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String strLine;

		while ((strLine = br.readLine()) != null) {
			list.add(strLine);
		}

		br.close();
		return list;
	}

	public void nextMarkov(int count, int lookahead) throws IOException {
		File cache = getCacheFile();
		Markov markov = new Markov(training.getAll(), lookahead);

		FileWriter fw = new FileWriter(cache, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);

		Random r = new Random();
		for (int i = 0; i < count; i++) {
			out.println(markov.nextString(r));
		}

		bw.close();
	}

	public MarkovTraining getTraining() {
		return training;
	}

	public void clear() throws IOException {
		PrintWriter writer = new PrintWriter(getCacheFile());
		writer.print("");
		writer.close();
	}

	@Override
	public int compareTo(@NonNull MarkovResults another) {
		return getTraining().compareTo(another.getTraining());
	}
}
