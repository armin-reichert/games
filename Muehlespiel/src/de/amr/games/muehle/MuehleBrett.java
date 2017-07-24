package de.amr.games.muehle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.easy.game.entity.GameEntity;

public class MuehleBrett extends GameEntity {

	public static final int ANZAHL_POSITIONEN = 24;

	private int width;
	private int height;
	private float[] xpos;
	private float[] ypos;
	private Richtung[][] matrix;
	private Steinfarbe[] belegung;

	public MuehleBrett(int width, int height) {
		this.width = width;
		this.height = height;
		belegung = new Steinfarbe[ANZAHL_POSITIONEN];
		graphAufbauen();
		zeichenPositionenBerechnen();
	}

	public void setzeStein(Steinfarbe stein, int pos) {
		belegung[pos] = stein;
	}

	private void graphAufbauen() {
		matrix = new Richtung[ANZAHL_POSITIONEN][ANZAHL_POSITIONEN];
		hLinie(0, 1);
		hLinie(1, 2);
		hLinie(3, 4);
		hLinie(4, 5);
		hLinie(6, 7);
		hLinie(7, 8);
		hLinie(9, 10);
		hLinie(10, 11);
		hLinie(12, 13);
		hLinie(13, 14);
		hLinie(15, 16);
		hLinie(16, 17);
		hLinie(18, 19);
		hLinie(19, 20);
		hLinie(21, 22);
		hLinie(22, 23);

		vLinie(0, 9);
		vLinie(9, 21);
		vLinie(3, 10);
		vLinie(10, 18);
		vLinie(6, 11);
		vLinie(11, 15);
		vLinie(1, 4);
		vLinie(4, 7);
		vLinie(16, 19);
		vLinie(19, 22);
		vLinie(8, 12);
		vLinie(12, 17);
		vLinie(5, 13);
		vLinie(13, 20);
		vLinie(2, 14);
		vLinie(14, 23);
	}

	private void hLinie(int punkt1, int punkt2) {
		matrix[punkt1][punkt2] = Richtung.HORIZONTAL;
		matrix[punkt2][punkt1] = Richtung.HORIZONTAL;
	}

	private void vLinie(int punkt1, int punkt2) {
		matrix[punkt1][punkt2] = Richtung.VERTIKAL;
		matrix[punkt2][punkt1] = Richtung.VERTIKAL;
	}

	private void zeichenPositionenBerechnen() {
		xpos = new float[ANZAHL_POSITIONEN];
		ypos = new float[ANZAHL_POSITIONEN];
		posX(0f, 0, 9, 21);
		posX(1f / 6, 3, 10, 18);
		posX(1f / 3, 6, 11, 15);
		posX(1f / 2, 1, 4, 7, 16, 19, 22);
		posX(2f / 3, 8, 12, 17);
		posX(5f / 6, 5, 13, 20);
		posX(1f, 2, 14, 23);
		posY(0f, 0, 1, 2);
		posY(1f / 6, 3, 4, 5);
		posY(1f / 3, 6, 7, 8);
		posY(1f / 2, 9, 10, 11, 12, 13, 14);
		posY(2f / 3, 15, 16, 17);
		posY(5f / 6, 18, 19, 20);
		posY(1f, 21, 22, 23);
	}

	private void posX(float x, int... positions) {
		for (int pos : positions) {
			xpos[pos] = x;
		}
	}

	private void posY(float y, int... positions) {
		for (int pos : positions) {
			ypos[pos] = y;
		}
	}

	public boolean sindHorizontalVerbunden(int p, int q) {
		return matrix[p][q] == Richtung.HORIZONTAL;
	}

	public boolean sindVertikalVerbunden(int p, int q) {
		return matrix[p][q] == Richtung.VERTIKAL;
	}

	public int anzNachbarn(int p, Richtung richtung) {
		int n = 0;
		for (int q = 0; q < ANZAHL_POSITIONEN; q += 1) {
			if (matrix[p][q] == richtung) {
				n += 1;
			}
		}
		return n;
	}

	public boolean istMühlenZentrum(int p, Steinfarbe farbe, Richtung richtung) {
		int w = 0;
		if (belegung[p] != farbe) {
			return false;
		}
		w = 1;
		for (int q = 0; q < ANZAHL_POSITIONEN; q += 1) {
			if (matrix[p][q] == richtung) {
				if (belegung[q] == farbe) {
					w += 1;
				}
			}
		}
		return w == 3;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());

		int radius = 5;

		// Linien zeichnen
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(radius / 2));
		for (int p = 0; p < ANZAHL_POSITIONEN; p += 1) {
			for (int q = 0; q < p; q += 1) {
				if (matrix[p][q] != null) {
					int x1 = Math.round(xpos[p] * width);
					int y1 = Math.round(ypos[p] * height);
					int x2 = Math.round(xpos[q] * width);
					int y2 = Math.round(ypos[q] * height);
					g.drawLine(x1, y1, x2, y2);
				}
			}
		}

		// Punkte zeichnen
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (int p = 0; p < ANZAHL_POSITIONEN; p += 1) {
			int x = Math.round(xpos[p] * width);
			int y = Math.round(ypos[p] * height);
			g.setColor(Color.BLACK);
			g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);

			// Belegung
			int steinGröße = 40;
			if (belegung[p] == Steinfarbe.WEISS) {
				g.setColor(Color.ORANGE);
				g.fillOval(x - steinGröße / 2, y - steinGröße / 2, steinGröße, steinGröße);
			} else if (belegung[p] == Steinfarbe.SCHWARZ) {
				g.setColor(Color.BLACK);
				g.fillOval(x - steinGröße / 2, y - steinGröße / 2, steinGröße, steinGröße);
			}

			// Infotext
			g.setColor(Color.RED);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
			String text = p + "";
			if (istMühlenZentrum(p, Steinfarbe.WEISS, Richtung.HORIZONTAL)) {
				text += " WH";
			}
			if (istMühlenZentrum(p, Steinfarbe.WEISS, Richtung.VERTIKAL)) {
				text += " WV";
			}
			if (istMühlenZentrum(p, Steinfarbe.SCHWARZ, Richtung.HORIZONTAL)) {
				text += " SH";
			}
			if (istMühlenZentrum(p, Steinfarbe.SCHWARZ, Richtung.VERTIKAL)) {
				text += " SV";
			}
			g.drawString(text, x + 2 * radius, y + 2 * radius);
		}

		g.translate(-tf.getX(), -tf.getY());
	}
}