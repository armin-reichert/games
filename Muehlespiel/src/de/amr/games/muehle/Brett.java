package de.amr.games.muehle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import de.amr.easy.game.entity.GameEntity;

public class Brett extends GameEntity {

	public static final int POSITIONEN = 24;

	private int width;
	private int height;
	private float[] xpos;
	private float[] ypos;

	private Richtung[][] verbindungen;
	private Stein[] belegung;

	public Brett(int width, int height) {
		this.width = width;
		this.height = height;
		belegung = new Stein[POSITIONEN];
		verbindungenErzeugen();
		berechneRelativeMalPositionen();
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public void setzeStein(Farbe farbe, int p) {
		Stein stein = new Stein(farbe);
		stein.tf.moveTo(xpos[p] * width, ypos[p] * height);
		belegung[p] = stein;
	}

	public void entferneStein(int p) {
		belegung[p] = null;
	}

	public Stein gibStein(int p) {
		return belegung[p];
	}

	public void leeren() {
		for (int p = 0; p < POSITIONEN; p += 1) {
			belegung[p] = null;
		}
	}

	private void verbindungenErzeugen() {
		verbindungen = new Richtung[POSITIONEN][POSITIONEN];

		horizontal(0, 1);
		horizontal(1, 2);
		horizontal(3, 4);
		horizontal(4, 5);
		horizontal(6, 7);
		horizontal(7, 8);
		horizontal(9, 10);
		horizontal(10, 11);
		horizontal(12, 13);
		horizontal(13, 14);
		horizontal(15, 16);
		horizontal(16, 17);
		horizontal(18, 19);
		horizontal(19, 20);
		horizontal(21, 22);
		horizontal(22, 23);

		vertikal(0, 9);
		vertikal(9, 21);
		vertikal(3, 10);
		vertikal(10, 18);
		vertikal(6, 11);
		vertikal(11, 15);
		vertikal(1, 4);
		vertikal(4, 7);
		vertikal(16, 19);
		vertikal(19, 22);
		vertikal(8, 12);
		vertikal(12, 17);
		vertikal(5, 13);
		vertikal(13, 20);
		vertikal(2, 14);
		vertikal(14, 23);
	}

	private void horizontal(int links, int rechts) {
		verbindungen[links][rechts] = Richtung.Osten;
		verbindungen[rechts][links] = Richtung.Westen;
	}

	private void vertikal(int oben, int unten) {
		verbindungen[oben][unten] = Richtung.Süden;
		verbindungen[unten][oben] = Richtung.Norden;
	}

	private void berechneRelativeMalPositionen() {
		xpos = new float[POSITIONEN];
		ypos = new float[POSITIONEN];
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

	public int findeBrettPosition(int x, int y, int radius) {
		for (int p = 0; p < POSITIONEN; p += 1) {
			int px = Math.round(xpos[p] * width);
			int py = Math.round(ypos[p] * height);
			int dx = px - x;
			int dy = py - y;
			double d = Math.sqrt(dx * dx + dy * dy);
			if (d <= radius) {
				return p;
			}
		}
		return -1;
	}

	public boolean inMühle(int p, Farbe farbe) {
		return findeMühle(p, farbe, true) != null || findeMühle(p, farbe, false) != null;
	}

	public Muehle findeMühle(int p, Farbe farbe, boolean horizontal) {

		// Liegt auf Position @p ein Stein der Farbe @farbe?
		if (belegung[p] == null) {
			return null;
		}
		Stein stein = belegung[p];
		if (stein.getFarbe() != farbe) {
			return null;
		}
		// "Ja"

		int q, r;

		// a) p -> q -> r
		q = findeNachbar(p, farbe, horizontal ? Richtung.Osten : Richtung.Süden);
		if (q != -1) {
			r = findeNachbar(q, farbe, horizontal ? Richtung.Osten : Richtung.Süden);
			if (r != -1) {
				return new Muehle(p, q, r, true);
			}
		}

		// b) q <- p -> r
		q = findeNachbar(p, farbe, horizontal ? Richtung.Westen : Richtung.Norden);
		if (q != -1) {
			r = findeNachbar(p, farbe, horizontal ? Richtung.Osten : Richtung.Süden);
			if (r != -1) {
				return new Muehle(q, p, r, true);
			}
		}

		// c) q <- r <- p
		r = findeNachbar(p, farbe, horizontal ? Richtung.Westen : Richtung.Norden);
		if (r != -1) {
			q = findeNachbar(r, farbe, horizontal ? Richtung.Westen : Richtung.Norden);
			if (q != -1) {
				return new Muehle(q, r, p, true);
			}
		}

		return null;
	}

	public int findeNachbar(int p, Richtung richtung) {
		for (int q = 0; q < POSITIONEN; q += 1) {
			if (verbindungen[p][q] == richtung) {
				return q;
			}
		}
		return -1; // kein Nachbar gefunden
	}

	private int findeNachbar(int p, Farbe farbe, Richtung richtung) {
		for (int q = 0; q < POSITIONEN; q += 1) {
			if (verbindungen[p][q] == richtung) {
				if (belegung[q] != null && belegung[q].getFarbe() == farbe) {
					// q ist Nachbar von p in Richtung @richtung und trägt Stein der Farbe @farbe
					return q;
				}
			}
		}
		return -1; // kein Nachbar gefunden
	}

	public Point gibMalPosition(int p) {
		int x = Math.round(xpos[p] * width);
		int y = Math.round(ypos[p] * height);
		return new Point(x, y);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());

		int radius = 5;

		// Linien zeichnen
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (int p = 0; p < POSITIONEN; p += 1) {
			int x1 = Math.round(xpos[p] * width);
			int y1 = Math.round(ypos[p] * height);
			for (int q = 0; q < p; q += 1) {
				if (verbindungen[p][q] != null) {
					int x2 = Math.round(xpos[q] * width);
					int y2 = Math.round(ypos[q] * height);
					g.setColor(Color.BLACK);
					g.setStroke(new BasicStroke(radius / 2));
					g.drawLine(x1, y1, x2, y2);
				}
			}
		}

		for (int p = 0; p < POSITIONEN; p += 1) {
			int x1 = Math.round(xpos[p] * width);
			int y1 = Math.round(ypos[p] * height);

			// Punkte zeichnen
			g.setColor(Color.BLACK);
			g.setFont(new Font("Arial", Font.PLAIN, 20));
			g.fillOval(x1 - radius, y1 - radius, 2 * radius, 2 * radius);
			g.drawString(p + "", x1 + 4 * radius, y1 + 4 * radius);
		}

		// Steine zeichnen
		for (int p = 0; p < POSITIONEN; p += 1) {
			if (belegung[p] != null) {
				belegung[p].draw(g);
			}
		}

		g.translate(-tf.getX(), -tf.getY());
	}
}