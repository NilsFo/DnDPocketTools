package de.wavegate.tos.dndpockettools.util.error;

import android.content.Context;

/**
 * Created by Nils on 13.05.2016.
 */
public class ImportCampaignException extends Exception {

	public ImportCampaignException(Throwable throwable) {
		super(throwable);
	}

	public ImportCampaignException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ImportCampaignException() {

	}

	public ImportCampaignException(int resID, Context context) {
		this(context.getResources().getString(resID));
	}

	public ImportCampaignException(String detailMessage) {
		super(detailMessage);
	}
}
