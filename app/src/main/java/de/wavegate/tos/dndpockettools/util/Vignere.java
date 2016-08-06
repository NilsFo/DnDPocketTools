package de.wavegate.tos.dndpockettools.util;

public class Vignere {
	private static char rot(char c, int i) {
		if (c == ' ')
			return ' ';
		boolean upperCase = c <= 90;
		if (!upperCase)
			c -= 32;
		c -= 65;
		if (i < 0)
			i += 26;
		c = (char) ((c + i) % 26);
		c += 65;
		if (!upperCase)
			c += 32;
		return c;
	}

	private static int getCharCode(char c) {
		if (!((c >= 65 && c <= 90) || (c >= 97 && c <= 122)))
			return -1;
		if (c > 90)
			c -= 32;
		c -= 65;
		return c;
	}

	public String vignereCrypt(String text, String keyString, boolean crypt) {
		String s = "";
		int[] keys = new int[keyString.length()];
		for (int i = 0; i < keys.length; i++)
			keys[i] = getCharCode(keyString.charAt(i));
		for (int i = 0; i < text.length(); i++) {
			int key = keys[i % keys.length];
			if (!crypt)
				key = -key;
			char c = text.charAt(i);
			char c2 = rot(c, key);
			if (getCharCode(c) != -1)
				s += c2;
			else
				s += c;
		}
		return s;
	}
}