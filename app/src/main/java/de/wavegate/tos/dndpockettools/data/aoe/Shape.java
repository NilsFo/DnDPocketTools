package de.wavegate.tos.dndpockettools.data.aoe;

/**
 * Created by Nils on 08.07.2016.
 */

public enum Shape {

	LINE, CUBE, CONE, SPHERE;

	public String toString() {
		switch (this) {
			case LINE:
				return "Line";
			case CUBE:
				return "Cube";
			case CONE:
				return "Cone";
			case SPHERE:
				return "Sphere";
		}
		return super.toString();
	}
}
