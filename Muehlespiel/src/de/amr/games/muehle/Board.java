package de.amr.games.muehle;

import static de.amr.games.muehle.Direction.NORTH;
import static de.amr.games.muehle.Direction.EAST;
import static de.amr.games.muehle.Direction.SOUTH;
import static de.amr.games.muehle.Direction.WEST;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;

/**
 * Das Mühlebrett.
 *
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class Board extends GameEntity {

	/** Anzahl Brettpositionen. */
	public static final int NUM_POS = 24;

	/* GRID[p] = { Nachbar(Norden), Nachbar(Osten), Nachbar(Süden), Nachbar(Westen) } */
	private static final int[][] GRID = {
			/*@formatter:off*/
			{ -1, 1, 9, -1 }, // Position 0: Nachbarpositionen: - (Norden), 1 (Osten), 9 (Süden), - (Westen) 
			{ -1, 2, 4, 0 }, 
			{ -1, -1, 14, 1 }, 
			{ -1, 4, 10, -1 },
			{ 1, 5, 7, 3 }, 
			{ -1, -1, 13, 4 },
			{ -1, 7, 11, -1 }, 
			{ 4, 8, -1, 6 }, 
			{ -1, -1, 12, 7 },
			{ 0, 10, 21, -1 }, 
			{ 3, 11, 18, 9 },
			{ 6, -1, 15, 10 }, 
			{ 8, 13, 17, -1 }, 
			{ 5, 14, 20, 12 }, 
			{ 2, -1, 23, 13 },
			{ 11, 16, -1, -1 },
			{ -1, 17, 19, 15 }, 
			{ 12, -1, -1, 16 }, 
			{ 10, 19, -1, -1 },
			{ 16, 20, 22, 18 }, 
			{ 13, -1, -1, 19 },
			{ 9, 22, -1, -1 }, 
			{ 19, 23, -1, 21 }, 
			{ 14, -1, -1, 22 },
			/*@formatter:on*/
	};

	/* (GRID_X[p], GRID[p]) is the relative grid position of position p */
	private static final int[] GRID_X = { 0, 3, 6, 1, 3, 5, 2, 3, 4, 0, 1, 2, 4, 5, 6, 2, 3, 4, 1, 3, 5, 0, 3, 6 };
	private static final int[] GRID_Y = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6 };

	private int width;
	private int height;
	private int posRadius;
	private Stone[] stones;

	public Board(int w, int h) {
		width = w;
		height = h;
		posRadius = w / 60;
		stones = new Stone[NUM_POS];
		Stone.radius = width / 24;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public IntStream positions() {
		return IntStream.range(0, NUM_POS);
	}

	public void clear() {
		stones = new Stone[NUM_POS];
	}

	public void placeStoneAt(int p, StoneColor color) {
		Stone stone = new Stone(color);
		stones[p] = stone;
		Point center = computeCenterPoint(p);
		stone.tf.moveTo(center.x, center.y);
	}

	public void removeStoneAt(int p) {
		stones[p] = null;
	}

	public Stone getStoneAt(int p) {
		return stones[p];
	}

	public boolean hasStoneAt(int p) {
		return getStoneAt(p) != null;
	}

	public Stream<Stone> stones() {
		return Stream.of(stones).filter(Objects::nonNull);
	}

	public IntStream neighbors(int p) {
		return IntStream.of(GRID[p]).filter(n -> n != -1);
	}

	public boolean hasEmptyNeighbor(int p) {
		return neighbors(p).anyMatch(q -> !hasStoneAt(q));
	}

	public boolean areNeighbors(int p, int q) {
		return neighbors(p).anyMatch(nb -> nb == q);
	}

	public boolean areNeighbors(int p, int q, Direction r) {
		return GRID[p][r.ordinal()] == q;
	}

	public int findNearestPosition(int x, int y, int radius) {
		return positions().filter(p -> {
			Point center = computeCenterPoint(p);
			int dx = center.x - x;
			int dy = center.y - y;
			return dx * dx + dy * dy <= radius * radius;
		}).findFirst().orElse(-1);
	}

	public boolean isInsideMill(int p, StoneColor color) {
		return findContainingMill(p, color, true) != null || findContainingMill(p, color, false) != null;
	}

	public Mill findContainingMill(int p, StoneColor color, boolean horizontal) {
		// Liegt auf Position @p ein Stein der Farbe @farbe?
		Stone stone = getStoneAt(p);
		if (stone == null || stone.getColor() != color) {
			return null;
		}

		int q, r;
		Direction left = horizontal ? WEST : NORTH;
		Direction right = horizontal ? EAST : SOUTH;

		// p -> q -> r
		q = findNeighbor(p, right, color);
		if (q != -1) {
			r = findNeighbor(q, right, color);
			if (r != -1) {
				return new Mill(p, q, r, true);
			}
		}

		// q <- p -> r
		q = findNeighbor(p, left, color);
		if (q != -1) {
			r = findNeighbor(p, right, color);
			if (r != -1) {
				return new Mill(q, p, r, true);
			}
		}

		// q <- r <- p
		r = findNeighbor(p, left, color);
		if (r != -1) {
			q = findNeighbor(r, left, color);
			if (q != -1) {
				return new Mill(q, r, p, true);
			}
		}

		return null;
	}

	public boolean allStonesOfColorInsideMills(StoneColor color) {
		/*@formatter:off*/
		return positions()
				.filter(this::hasStoneAt)
				.filter(p -> getStoneAt(p).getColor() == color)
				.allMatch(p -> isInsideMill(p, color));
		/*@formatter:on*/
	}

	public int findNeighbor(int p, Direction r) {
		/*@formatter:off*/
		return positions()
				.filter(q -> areNeighbors(p, q, r))
				.findAny()
				.orElse(-1);
		/*@formatter:on*/
	}

	private int findNeighbor(int p, Direction r, StoneColor color) {
		/*@formatter:off*/
		return positions()
				.filter(q -> areNeighbors(p, q, r))
				.filter(this::hasStoneAt)
				.filter(q -> getStoneAt(q).getColor() == color)
				.findAny()
				.orElse(-1);
		/*@formatter:on*/
	}

	public Point computeCenterPoint(int p) {
		return new Point(GRID_X[p] * width / 6, GRID_Y[p] * height / 6);
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
			Point from = computeCenterPoint(p);
			neighbors(p).forEach(q -> {
				Point to = computeCenterPoint(q);
				g.drawLine(from.x, from.y, to.x, to.y);
			});
		}

		// Positionen
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.PLAIN, 20));
		for (int p = 0; p < NUM_POS; p += 1) {
			Point pos = computeCenterPoint(p);
			g.fillOval(pos.x - posRadius, pos.y - posRadius, 2 * posRadius, 2 * posRadius);
			g.drawString(p + "", pos.x + 3 * posRadius, pos.y + 3 * posRadius);
		}

		// Steine
		stones().forEach(stone -> stone.draw(g));

		g.translate(-tf.getX(), -tf.getY());
	}

	public void moveStone(int from, int to) {
		if (stones[from] == null) {
			throw new IllegalStateException("Startposition muss einen Stein enthalten");
		}
		if (stones[to] != null) {
			throw new IllegalStateException("Zielposition muss leer sein");
		}
		Stone stone = stones[from];
		stones[to] = stone;
		Point toPoint = computeCenterPoint(to);
		stone.tf.moveTo((float) toPoint.getX(), (float) toPoint.getY());
		stones[from] = null;
	}
}