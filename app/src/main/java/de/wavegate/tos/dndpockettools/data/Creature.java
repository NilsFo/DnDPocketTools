package de.wavegate.tos.dndpockettools.data;

/**
 * Created by Nils on 06.04.2016.
 */
public class Creature {

	private String name;
	private int initiative;
	private int AC;

	public Creature() {
		this("", 0, 0);
	}

	public Creature(String name, int initiative, int AC) {
		this.name = name;
		this.initiative = initiative;
		this.AC = AC;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getInitiative() {
		return initiative;
	}

	public void setInitiative(int initiative) {
		this.initiative = initiative;
	}

	public int getAC() {
		return AC;
	}

	public void setAC(int AC) {
		this.AC = AC;
	}
}
