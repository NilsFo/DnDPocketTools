package de.wavegate.tos.dndpockettools.activity.view.dialogfragment;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import de.wavegate.tos.dndpockettools.R;
import de.wavegate.tos.dndpockettools.data.Campaign;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 22.04.2016.
 */
public class ShareCampaignFragment extends DialogFragment {

	public static final int QR_IMAGE_SIZE = 512;

	private Campaign campaign;
	private String qrcode;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle(R.string.action_share);
		builder.setIcon(R.drawable.ic_menu_share);
		if (savedInstanceState != null) {
			dismiss();
		}
		if (campaign == null) {
			Log.w(LOGTAG, "Wanted to Share Campaign data. But no campaign to share was selected (camapign == null).");
			dismiss();
			return builder.create();
		}
		builder.setTitle(campaign.getName());
		qrcode = campaign.toQRString(getContext());

		builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		View view = inflater.inflate(R.layout.dialog_share_campaign, null);

		ImageView qr = (ImageView) view.findViewById(R.id.share_campaign_qr_view);
		Button copyBT = (Button) view.findViewById(R.id.share_campaign_copy_bt);

		qr.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("My campaign", qrcode);
				clipboard.setPrimaryClip(clip);

				Toast.makeText(getContext(), R.string.campaign_copied_to_clipboard, Toast.LENGTH_LONG).show();
			}
		});

		QRCodeWriter writer = new QRCodeWriter();
		try {
			BitMatrix bitMatrix = writer.encode(qrcode, BarcodeFormat.QR_CODE, QR_IMAGE_SIZE, QR_IMAGE_SIZE);
			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
				}
			}
			qr.setImageBitmap(bmp);
		} catch (WriterException e) {
			e.printStackTrace();
			Log.e(LOGTAG, "Error while creating the QR code for " + qrcode, e);
			dismiss();

			//TODO Error message
		}

		builder.setView(view);
		return builder.create();
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}
}
