package de.wavegate.tos.dndpockettools.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 08.04.2016.
 */
public class AlignmentBackground extends Drawable {

	public static final int BASE_R = 100;
	public static final int BASE_G = 25;
	public static final int BASE_B = 100;
	public static final int BORDER_MARGIN = 5;

	private double percentage;
	private Paint borderColor;
	private Paint backgroundColor;

	public AlignmentBackground(double percentage) {
		this.percentage = percentage;
		borderColor = new Paint();
		borderColor.setColor(Color.BLACK);

		int r = BASE_R;
		int g = BASE_G;
		int b = BASE_B;

		r = (int) (r + (percentage * (255 - r)));
		g = (int) (g + (percentage * (255 - g)));
		b = (int) (b + (percentage * (255 - b)));

		backgroundColor = new Paint();
		backgroundColor.setARGB(255, r, g, b);
		Log.i(LOGTAG, "Backround for: " + percentage * 100 + "% tint results in R:" + r + " G:" + g + " B:" + b);
	}

	@Override
	public void draw(Canvas canvas) {
		int w = canvas.getWidth();
		int h = canvas.getHeight();

		canvas.drawRect(0, 0, w, h, borderColor);
		canvas.drawRect(BORDER_MARGIN, BORDER_MARGIN, w - BORDER_MARGIN, h - BORDER_MARGIN, backgroundColor);
	}

	@Override
	public void setAlpha(int alpha) {

	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {

	}

	@Override
	public int getOpacity() {
		return 0;
	}

	public double getPercentage() {
		return percentage;
	}
}
