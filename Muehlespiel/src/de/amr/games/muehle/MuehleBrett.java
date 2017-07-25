package de.amr.games.muehle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.easy.game.entity.GameEntity;

public class MuehleBrett extends GameEntity {

	public static final int POSITIONEN = 24;

	private int width;
	private int height;
	private float[] xpos;
	private float[] ypos;

	private Richtung[][] verbindungen;
	private MuehleStein[] belegung;

	public MuehleBrett(int width, int height) {
		this.width = width;
		this.height = height;
		belegung = new MuehleStein[POSITIONEN];
		verbindungenErzeugen();
		zeichenPositionenBerechnen();
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public void setzeStein(Steinfarbe farbe, int pos) {
		MuehleStein stein = new MuehleStein(farbe);
		stein.tf.moveTo(xpos[pos] * width, ypos[pos] * height);
		belegung[pos] = stein;
	}

	public MuehleStein gibStein(int p) {
		return belegung[p];
	}

	public void leeren() {
		for (int p = 0; p < POSITIONEN; p += 1) {
			belegung[p] = null;
		}
	}

	private void verbindungenErzeugen() {
		verbindungen = new Richtung[POSITIONEN][POSITIONEN];
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

	private void hLinie(int links, int rechts) {
		verbindungen[links][rechts] = Richtung.Osten;
		verbindungen[rechts][links] = Richtung.Westen;
	}

	private void vLinie(int oben, int unten) {
		verbindungen[oben][unten] = Richtung.Süden;
		verbindungen[unten][oben] = Richtung.Norden;
	}

	private void zeichenPositionenBerechnen() {
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

	public int findePosition(int x, int y, int radius) {
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

	public boolean sindHorizontalVerbunden(int p, int q) {
		return verbindungen[p][q] == Richtung.Westen || verbindungen[p][q] == Richtung.Osten;
	}

	public boolean sindVertikalVerbunden(int p, int q) {
		return verbindungen[p][q] == Richtung.Norden || verbindungen[p][q] == Richtung.Süden;
	}

	public int anzNachbarn(int p, Richtung richtung) {
		int n = 0;
		for (int q = 0; q < POSITIONEN; q += 1) {
			if (verbindungen[p][q] == richtung) {
				n += 1;
			}
		}
		return n;
	}

	public Muehle findeHorizontaleMühle(int p, Steinfarbe farbe) {

		// Liegt auf Position @p ein Stein der Farbe @farbe?
		if (belegung[p] == null) {
			return null;
		}
		MuehleStein stein = belegung[p];
		if (stein.getFarbe() != farbe) {
			return null;
		}
		// "Ja"

		int q, r;

		// a) p -> q -> r
		q = findeNachbar(p, farbe, Richtung.Osten);
		if (q != -1) {
			r = findeNachbar(q, farbe, Richtung.Osten);
			if (r != -1) {
				return new Muehle(p, q, r, true);
			}
		}

		// b) q <- p -> r
		q = findeNachbar(p, farbe, Richtung.Westen);
		if (q != -1) {
			r = findeNachbar(p, farbe, Richtung.Osten);
			if (r != -1) {
				return new Muehle(q, p, r, true);
			}
		}

		// c) q <- r <- p
		r = findeNachbar(p, farbe, Richtung.Westen);
		if (r != -1) {
			q = findeNachbar(r, farbe, Richtung.Westen);
			if (q != -1) {
				return new Muehle(q, r, p, true);
			}
		}

		return null;
	}

	public Muehle findeVertikaleMühle(int p, Steinfarbe farbe) {

		// Liegt auf Position @p ein Stein der Farbe @farbe?
		if (belegung[p] == null) {
			return null;
		}
		MuehleStein stein = belegung[p];
		if (stein.getFarbe() != farbe) {
			return null;
		}
		// "Ja"

		int q, r;

		// a) p -> q -> r
		q = findeNachbar(p, farbe, Richtung.Süden);
		if (q != -1) {
			r = findeNachbar(q, farbe, Richtung.Süden);
			if (r != -1) {
				return new Muehle(p, q, r, false);
			}
		}

		// b) q <- p -> r
		q = findeNachbar(p, farbe, Richtung.Norden);
		if (q != -1) {
			r = findeNachbar(p, farbe, Richtung.Süden);
			if (r != -1) {
				return new Muehle(q, p, r, false);
			}
		}

		// c) q <- r <- p
		r = findeNachbar(p, farbe, Richtung.Norden);
		if (r != -1) {
			q = findeNachbar(r, farbe, Richtung.Norden);
			if (q != -1) {
				return new Muehle(q, r, p, false);
			}
		}

		return null;
	}

	int findeNachbar(int p, Steinfarbe farbe, Richtung richtung) {
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

	public boolean istMühlenZentrum(int p, Steinfarbe farbe, Richtung richtung) {
		if (belegung[p] == null || belegung[p].getFarbe() != farbe) {
			return false;
		}
		int gleicheSteine = 1;
		for (int q = 0; q < POSITIONEN; q += 1) {
			if (verbindungen[p][q] == richtung) {
				if (belegung[q] != null && belegung[q].getFarbe() == farbe) {
					gleicheSteine += 1;
				}
			}
		}
		return gleicheSteine == 3;
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