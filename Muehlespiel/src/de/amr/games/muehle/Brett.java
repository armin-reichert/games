package de.amr.games.muehle;

import static de.amr.games.muehle.Richtung.Norden;
import static de.amr.games.muehle.Richtung.Osten;
import static de.amr.games.muehle.Richtung.Süden;
import static de.amr.games.muehle.Richtung.Westen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;

/**
 * Das Mühlebrett.
 *
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class Brett extends GameEntity {

	/** Anzahl Brettpositionen. */
	public static final int NUM_POS = 24;

	/* GRID[p] = {Norden, Osten, Süden, Westen} */
	private static final int[][] GRID = {
			/*@formatter:off*/
			{ -1, 1, 9, -1 }, // 0
			{ -1, 2, 4, 0 }, 
			{ -1, -1, 14, 1 }, 
			{ -1, 4, 10, -1 },
			{ 1, 5, 7, 3 }, 
			{ -1, -1, 13, 4 }, // 5
			{ -1, 7, 11, -1 }, 
			{ 4, 8, -1, 6 }, 
			{ -1, -1, 12, 7 },
			{ 0, 10, 21, -1 }, 
			{ 3, 11, 18, 9 }, // 10
			{ 6, -1, 15, 10 }, 
			{ 8, 13, 17, -1 }, 
			{ 5, 14, 20, 12 }, 
			{ 2, -1, 23, 13 },
			{ 11, 16, -1, -1 }, // 15
			{ -1, 17, 19, 15 }, 
			{ 12, -1, -1, 16 }, 
			{ 10, 19, -1, -1 },
			{ 16, 20, 22, 18 }, 
			{ 13, -1, -1, 19 }, // 20
			{ 9, 22, -1, -1 }, 
			{ 19, 23, -1, 21 }, 
			{ 14, -1, -1, 22 }, // 23
			/*@formatter:on*/
	};

	private static final int[][] GRID_DRAW_POSITION = {
			/*@formatter:off*/
			{0,0}, {3,0}, {6,0},	
			{1,1}, {3,1}, {5,1},	
			{2,2}, {3,2}, {4,2},	
			{0,3}, {1,3}, {2,3}, {4,3}, {5,3}, {6,3},
			{2,4}, {3,4}, {4,4},	
			{1,5}, {3,5}, {5,5},	
			{0,6}, {3,6}, {6,6},	
			/*@formatter:on*/
	};

	private int width;
	private int height;
	private int posRadius;
	private Stein[] content;

	public Brett(int w, int h) {
		width = w;
		height = h;
		posRadius = 5;
		content = new Stein[NUM_POS];
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public void placeStone(SteinFarbe color, int p) {
		Stein stone = new Stein(color, width / 24);
		Point pos = getDrawPosition(p);
		stone.tf.moveTo(pos.x, pos.y);
		content[p] = stone;
	}

	public void removeStone(int p) {
		content[p] = null;
	}

	public Stein getStone(int p) {
		return content[p];
	}

	public Stream<Stein> getStones() {
		return Stream.of(content).filter(c -> c != null);
	}

	public void clear() {
		content = new Stein[NUM_POS];
	}

	public boolean areNeighbors(int p, int q) {
		return Stream.of(Richtung.values()).anyMatch(r -> areNeighbors(p, q, r));
	}

	public IntStream getNeighbors(int p) {
		return IntStream.of(GRID[p]).filter(n -> n != -1);
	}

	public boolean areNeighbors(int p, int q, Richtung r) {
		return GRID[p][r.ordinal()] == q;
	}

	public int findNearestPosition(int x, int y, int radius) {
		for (int p = 0; p < NUM_POS; p += 1) {
			Point pos = getDrawPosition(p);
			int dx = pos.x - x;
			int dy = pos.y - y;
			if (dx * dx + dy * dy <= radius * radius) {
				return p;
			}
		}
		return -1;
	}

	public boolean insideMill(int p, SteinFarbe color) {
		return findMill(p, color, true) != null || findMill(p, color, false) != null;
	}

	public Muehle findMill(int p, SteinFarbe color, boolean horizontal) {
		// Liegt auf Position @p ein Stein der Farbe @farbe?
		Stein stone = getStone(p);
		if (stone == null || stone.getColor() != color) {
			return null;
		}

		int q, r;
		Richtung left = horizontal ? Westen : Norden;
		Richtung right = horizontal ? Osten : Süden;

		// p -> q -> r
		q = findNeighbor(p, right, color);
		if (q != -1) {
			r = findNeighbor(q, right, color);
			if (r != -1) {
				return new Muehle(p, q, r, true);
			}
		}

		// q <- p -> r
		q = findNeighbor(p, left, color);
		if (q != -1) {
			r = findNeighbor(p, right, color);
			if (r != -1) {
				return new Muehle(q, p, r, true);
			}
		}

		// q <- r <- p
		r = findNeighbor(p, left, color);
		if (r != -1) {
			q = findNeighbor(r, left, color);
			if (q != -1) {
				return new Muehle(q, r, p, true);
			}
		}

		return null;
	}

	public int findNeighbor(int p, Richtung r) {
		for (int q = 0; q < NUM_POS; q += 1) {
			if (areNeighbors(p, q, r)) {
				return q;
			}
		}
		return -1; // kein Nachbar gefunden
	}

	private int findNeighbor(int p, Richtung r, SteinFarbe color) {
		for (int q = 0; q < NUM_POS; q += 1) {
			if (areNeighbors(p, q, r)) {
				Stein stone = getStone(q);
				if (stone != null && stone.getColor() == color) {
					// q ist Nachbar von p in Richtung @r und trägt Stein der Farbe @color
					return q;
				}
			}
		}
		return -1; // kein Nachbar gefunden
	}

	public Point getDrawPosition(int p) {
		int x = GRID_DRAW_POSITION[p][0] * width / 6;
		int y = GRID_DRAW_POSITION[p][1] * height / 6;
		return new Point(x, y);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Hintergrund
		g.setColor(new Color(255, 255, 224));
		g.fillRect(0, 0, width, height);

		// Linien
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(posRadius / 2));
		for (int p = 0; p < NUM_POS; p += 1) {
			Point from = getDrawPosition(p);
			getNeighbors(p).forEach(q -> {
				Point to = getDrawPosition(q);
				g.drawLine(from.x, from.y, to.x, to.y);
			});
		}

		// Positionen
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.PLAIN, 20));
		for (int p = 0; p < NUM_POS; p += 1) {
			Point pos = getDrawPosition(p);
			g.fillOval(pos.x - posRadius, pos.y - posRadius, 2 * posRadius, 2 * posRadius);
			g.drawString(p + "", pos.x + 4 * posRadius, pos.y + 4 * posRadius);
		}

		// Steine
		getStones().forEach(stone -> stone.draw(g));

		g.translate(-tf.getX(), -tf.getY());
	}
}