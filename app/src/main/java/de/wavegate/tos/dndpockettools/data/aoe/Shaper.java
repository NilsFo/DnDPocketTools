package de.wavegate.tos.dndpockettools.data.aoe;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

import static de.wavegate.tos.dndpockettools.MainActivity.LOGTAG;

/**
 * Created by Nils on 08.07.2016.
 */

public class Shaper {

	private int size, width, height;
	private Shape shape;
	private boolean normalized;

	private ArrayList<Point> pointList;

	public Shaper(int size, Shape shape) {
		this.size = size;
		this.shape = shape;
		normalized = false;

		pointList = new ArrayList<>();

		createShape();
	}

	private void createShape() {
		int i = size / 5;
		addPoint(new Point(0, 0));

		if (i > 1)
			switch (shape) {
				case LINE:
					shapeLine(i);
					break;
				case CONE:
					shapeCone(i, 0, 0, true);
					break;
				case CUBE:
					shapeCube(i);
					break;
				case SPHERE:
					shapeSphere(i - 1);
					break;
			}
	}

	private void shapeCone(int stepsLeft, int xPos, int yPos, boolean spread) {
		Log.i(LOGTAG, "Coning. Steps left: " + stepsLeft);
		if (stepsLeft <= 0) return;
		Point p = new Point(xPos, yPos);
		addPoint(p);

		shapeCone(stepsLeft - 1, xPos, yPos + 1, !spread);
		if (spread) {
			shapeCone(stepsLeft - 1, xPos - 1, yPos + 1, !spread);
			shapeCone(stepsLeft - 1, xPos + 1, yPos + 1, !spread);
		}
	}

	private void shapeSphere(int steps) {
		//int x = steps;
		//int y = 0;
		//int x0 = 0;
		//int y0 = 0;
		//int err = 0;
//
		//while (x >= y) {
		//	addPoint(new Point(x0 + x, y0 + y));
		//	addPoint(new Point(x0 + y, y0 + x));
		//	addPoint(new Point(x0 - y, y0 + x));
		//	addPoint(new Point(x0 - x, y0 + y));
		//	addPoint(new Point(x0 - x, y0 - y));
		//	addPoint(new Point(x0 - y, y0 - x));
		//	addPoint(new Point(x0 + y, y0 - x));
		//	addPoint(new Point(x0 + x, y0 - y));
//
		//	y += 1;
		//	err += 1 + 2 * y;
		//	if (2 * (err - x) + 1 > 0) {
		//		x -= 1;
		//		err += 1 - 2 * x;
		//	}
		//}
//
		//ArrayList<Point> list = new ArrayList<>();
		//for (Point p : pointList) if (p.x > 0 && p.y > 0) list.add(p);
//
		//for (Point p : list) {
		//	for (int i = 0; i < p.x + 1; i++) {
		//		for (int j = 0; j < p.y + 1; j++) {
		//			addPoint(new Point(i, j));
		//			addPoint(new Point(i * -1, j));
		//			addPoint(new Point(i, j * -1));
		//			addPoint(new Point(i * -1, j * -1));
		//		}
		//	}
		//}

		shapeQuarterCircle(steps, 0, 0);
	}

	private void shapeQuarterCircle(double stepsLeft, int x, int y) {
		int i = (int) stepsLeft;
		if (i==0)return;

		addPoint(new Point(x,y));

		double steps = Math.ceil(stepsLeft);

	}

	private void shapeCube(int steps) {
		for (int i = 0; i < steps; i++) {
			for (int j = 0; j < steps; j++) {
				addPoint(new Point(i, j));
			}
		}
	}

	private void shapeLine(int size) {
		for (int i = 0; i < size; i++) {
			addPoint(new Point(0, i));
		}
	}

	public int getWidth() {
		int width = 0;
		for (Point p : pointList) {
			if (p.x > width) width = p.x;
		}

		return width;
	}

	private boolean addPoint(Point point) {
		for (Point p : pointList) {
			if (p.equals(point.x, point.y)) return false;
		}
		return pointList.add(point);
	}

	public int getHeight() {
		int height = 0;
		for (Point p : pointList) {
			if (p.y > height) height = p.y;
		}

		return height;
	}

	public void normalize() {
		int x = getMaxX();
		int y = getMaxY();

		if (normalized) {
			Log.w(LOGTAG, "Wanted to normalize this Shaper, but normalisation was already done!");
			return;
		}

		switch (shape) {
			case LINE:
				offset(0, 1);
				break;
		}
	}

	public void offset(int dx, int dy) {
		for (Point p : pointList) p.offset(dx, dy);
	}

	public boolean hasPoint(int x, int y) {
		for (Point p : pointList) {
			if (p.equals(x, y)) return true;
		}
		return false;
	}

	public int getTileCount() {
		return pointList.size();
	}

	public int getMaxX() {
		int x = 0;
		for (Point p : pointList) {
			if (p.x > x) x = p.x;
		}
		return x;
	}

	public int getMaxY() {
		int y = 0;
		for (Point p : pointList) {
			if (p.x > y) y = p.y;
		}
		return y;
	}

	public int getSize() {
		return size;
	}

	public Shape getShape() {
		return shape;
	}
}
