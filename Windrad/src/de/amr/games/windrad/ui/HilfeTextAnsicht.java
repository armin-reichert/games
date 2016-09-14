package de.amr.games.windrad.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class HilfeTextAnsicht {

	private static final String[][] TEXTE = {
		/*@formatter:off*/
		{"F1", "Hilfe ein/aus"},
		{"SPACE", "Windrad starten/stoppen"},
		{"Shift SPACE", "Alle Windräder starten"},
		{"S", "Nächstes Windrad auswählen"},
		{"N", "Neues Windrad bauen"},
		{"I", "Drehrichtung umkehren"},
		{"UP", "Windrad nach oben schieben"},
		{"DOWN", "Windrad nach unten schieben"},
		{"LEFT", "Windrad nach links schieben"},
		{"RIGHT", "Windrad nach rechts schieben"},
		{"Z", "Zwicky-Windrad (mini)"},
		{"+", "Windradhöhe vergrößern"},
		{"-", "Windradhöhe verkleinern"},
		{"Control +", "Rotorlänge vergrößern"},
		{"Control -", "Rotorlänge verkleinern"},
		{"Shift +", "Nabenradius vergrößern"},
		{"Shift -", "Nabenradius verkleinern"},
		/*@formatter:on*/
	};

	private int x, y;
	private Font font;

	public HilfeTextAnsicht() {
		font = new Font("Monospaced", Font.PLAIN, 14);
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void draw(Graphics2D g) {
		int lineHeight = font.getSize() * 120 / 100;
		g.setColor(new Color(0, 0, 50, 30));
		g.fillRoundRect(x, y - font.getSize(), 350, lineHeight * (TEXTE.length + 1), 20, 20);
		g.setFont(font);
		g.setColor(Color.WHITE);
		for (String[] text : TEXTE) {
			g.drawString(String.format("%10s", text[0]) + " = " + text[1], x, y);
			y += lineHeight;
		}
	}
}
