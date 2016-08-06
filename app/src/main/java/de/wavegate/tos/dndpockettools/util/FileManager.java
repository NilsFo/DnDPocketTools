package de.wavegate.tos.dndpockettools.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import de.wavegate.tos.dndpockettools.R;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 27.04.2016.
 */
public class FileManager {

	public static final String NO_MEDIA = ".nomedia";
	public static final String CAMPAIGN_BACKUP_DIR_NAME = "campaign backup";
	public static final String DIR_NAME_GENERATOR_SOURCES = "name generator";

	private Context context;

	public FileManager(Context context) {
		this.context = context;
	}

	public File getOriginDir() {
		File f = Environment.getExternalStorageDirectory();
		//TODO does this parent file thing work?

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
		}
		f = new File(f, context.getString(R.string.app_name));

		Log.v(LOGTAG, "Requested Origin File. Location: " + f.getAbsolutePath());
		if (!f.exists()) {
			if (!f.mkdirs()) {
				Log.e(LOGTAG, "Directory not created");
			} else {
				Log.v(LOGTAG, "Origin Dir was created without problems.");
			}
		} else {
			Log.v(LOGTAG, "Origin Dir already exists. No need to create.");
		}
		createNoMediaFile(f);

		return f;
	}

	public void deleteCache() {
		File dir = getCacheDir();
		try {
			deleteDir(dir);
		} catch (Exception e) {
			e.printStackTrace();
			Log.i(LOGTAG, "Failed to delete the cache dir: " + dir.getAbsolutePath(), e);
		}
	}

	public File getCacheDir() {
		return context.getCacheDir();
	}

	public File getNameGeneratorCache() {
		File f = new File(getCacheDir(), DIR_NAME_GENERATOR_SOURCES);

		if (!f.exists()) {
			boolean works = f.mkdirs();
			works |= createNoMediaFile(f);

			if (!works) {
				Log.v(LOGTAG, "Errors regarding the cache dir. Either it could not have been created, or it had problems with its NOMEDIA file.");
			}
		}
		return f;
	}

	public File getCampaignBackupDir() {
		File f = new File(getOriginDir(), CAMPAIGN_BACKUP_DIR_NAME);
		if (!f.exists()) f.mkdirs();
		createNoMediaFile(f);

		return f;
	}

	public File getNameGeneratorSources() {
		File f = new File(getOriginDir(), DIR_NAME_GENERATOR_SOURCES);
		if (!f.exists()) f.mkdirs();
		createNoMediaFile(f);

		return f;
	}

	public void browseFolder(File file) {
		//Log.i(LOGTAG, "Requesting to browse file: " + file.getAbsolutePath());
		////Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		//Intent intent = new Intent(Intent.ACTION_VIEW);
		////Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
		////		+ "/myFolder/");
//
		//intent.setDataAndType(Uri.parse(file.getAbsolutePath()), "resource/folder");
		//context.startActivity(Intent.createChooser(intent, "Open folder"));
		//Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + "/myFolder/");
		//Intent intent = new Intent(Intent.ACTION_VIEW);
		//intent.setDataAndType(selectedUri, "resource/folder");
		//context.startActivity(Intent.createChooser(intent, "Open folder"));
		String folderPath = file.getAbsolutePath();

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		Uri myUri = Uri.parse(folderPath);
		intent.setDataAndType(myUri, "file/*");
		context.startActivity(intent);
	}

	public boolean createNoMediaFile(File parent) {
		if (!parent.exists() || !parent.isDirectory()) return false;

		File f = new File(parent, NO_MEDIA);
		if (f.exists()) return true;

		try {
			return f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(LOGTAG, "Wanted to create 'NomediaFile' in " + parent.getAbsolutePath() + " but it failed!", e);
			return false;
		}
	}

	public boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (String aChildren : children) {
				boolean success = deleteDir(new File(dir, aChildren));
				if (!success) {
					return false;
				}
			}
			return dir.delete();
		} else
			return dir != null && dir.isFile() && dir.delete();
	}
}
