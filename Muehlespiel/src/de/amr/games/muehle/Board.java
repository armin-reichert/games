package de.amr.games.muehle;

import static de.amr.easy.game.math.Vector2.dist;
import static de.amr.games.muehle.Direction.EAST;
import static de.amr.games.muehle.Direction.NORTH;
import static de.amr.games.muehle.Direction.SOUTH;
import static de.amr.games.muehle.Direction.WEST;
import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;

/**
 * Das Mühlebrett.
 *
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class Board extends GameEntity {

	public static final int NUM_POS = 24;

	/* NEIGHBORS[p] = { Nachbar(Norden), Nachbar(Osten), Nachbar(Süden), Nachbar(Westen) } */
	private static final int[][] NEIGHBORS = {
			/*@formatter:off*/
			{ -1, 1, 9, -1 }, // Position 0: - (Norden), 1 (Osten), 9 (Süden), - (Westen) 
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

	/*
	 * (GRID_X[p], GRID_Y[p]) is the grid coordinate of position p in the board's [0..6] x [0..6]
	 * grid.
	 */
	private static final int[] GRID_X = { 0, 3, 6, 1, 3, 5, 2, 3, 4, 0, 1, 2, 4, 5, 6, 2, 3, 4, 1, 3, 5, 0, 3, 6 };
	private static final int[] GRID_Y = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6 };

	private int width;
	private int height;
	private int posRadius;
	private Stone[] stones;
	private boolean showPositionNumbers;

	public Board(int w, int h) {
		width = w;
		height = h;
		posRadius = w / 60;
		stones = new Stone[NUM_POS];
		Stone.radius = width / 24;
	}

	// Keyboard shortcuts

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			showPositionNumbers = !showPositionNumbers;
		}
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public int findPosition(int x, int y) {
		int boardX = abs(round(x - tf.getX()));
		int boardY = abs(round(y - tf.getY()));
		return findNearestPosition(boardX, boardY, getWidth() / 18);
	}

	// Allocation related methods and predicates:

	public void clear() {
		stones = new Stone[NUM_POS];
	}

	public void putStoneAt(int p, StoneColor color) {
		if (stones[p] != null) {
			throw new IllegalStateException("Zielposition muss leer sein");
		}
		Stone stone = new Stone(color);
		stone.tf.moveTo(centerPoint(p));
		stones[p] = stone;
	}

	public void removeStoneAt(int p) {
		stones[p] = null;
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
		stone.tf.moveTo(centerPoint(to));
		stones[from] = null;
	}

	public Stone getStoneAt(int p) {
		return stones[p];
	}

	public boolean isEmpty(int p) {
		return stones[p] == null;
	}

	public boolean hasStoneAt(int p) {
		return stones[p] != null;
	}

	public IntStream positions() {
		return IntStream.range(0, NUM_POS);
	}

	public IntStream positions(StoneColor color) {
		return positions().filter(this::hasStoneAt).filter(p -> getStoneAt(p).getColor() == color);
	}

	public Stream<Stone> stones() {
		return Stream.of(stones).filter(Objects::nonNull);
	}

	public Stream<Stone> stones(StoneColor color) {
		return stones().filter(stone -> stone.getColor() == color);
	}

	public int numStones(StoneColor color) {
		return (int) stones(color).count();
	}

	public IntStream neighbors(int p) {
		return IntStream.of(NEIGHBORS[p]).filter(n -> n != -1);
	}

	public int findNeighbor(int p, Direction dir) {
		return NEIGHBORS[p][dir.ordinal()];
	}

	private int findNeighbor(int p, Direction dir, StoneColor color) {
		int n = findNeighbor(p, dir);
		return n != -1 && hasStoneAt(n) && getStoneAt(n).getColor() == color ? n : -1;
	}

	public boolean hasEmptyNeighbor(int p) {
		return neighbors(p).anyMatch(this::isEmpty);
	}

	public boolean canMoveFrom(int p) {
		return hasStoneAt(p) && hasEmptyNeighbor(p);
	}

	public boolean cannotMoveFrom(StoneColor color) {
		return positions(color).allMatch(p -> emptyNeighbors(p).count() == 0);
	}

	public IntStream allMovableStonePositions(StoneColor color) {
		return positions(color).filter(this::hasEmptyNeighbor);
	}

	public IntStream emptyNeighbors(int p) {
		return neighbors(p).filter(this::isEmpty);
	}

	public boolean areNeighbors(int p, int q) {
		return neighbors(p).anyMatch(n -> n == q);
	}

	public boolean areNeighbors(int p, int q, Direction dir) {
		return NEIGHBORS[p][dir.ordinal()] == q;
	}

	public Direction getDirection(int p, int q) {
		return Stream.of(Direction.values()).filter(dir -> areNeighbors(p, q, dir)).findFirst().orElse(null);
	}

	public int findNearestPosition(int x, int y, int radius) {
		return positions().filter(p -> dist(centerPoint(p), new Vector2(x, y)) <= radius).findFirst().orElse(-1);
	}

	public boolean isPositionInsideMill(int p, StoneColor color) {
		return findContainingMill(p, color, true) != null || findContainingMill(p, color, false) != null;
	}

	/**
	 * Tells if all stones of the given color are inside some mill.
	 * 
	 * @param color
	 *          stone color
	 * @return if all stones of given color are inside some mill
	 */
	public boolean areAllStonesInsideMill(StoneColor color) {
		return positions(color).allMatch(p -> isPositionInsideMill(p, color));
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

	// Draw related methods

	public Vector2 centerPoint(int p) {
		return new Vector2(GRID_X[p] * width / 6, GRID_Y[p] * height / 6);
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
		positions().forEach(p -> {
			Vector2 centerFrom = centerPoint(p);
			neighbors(p).forEach(q -> {
				Vector2 centerTo = centerPoint(q);
				g.drawLine(centerFrom.roundedX(), centerFrom.roundedY(), centerTo.roundedX(), centerTo.roundedY());
			});
		});

		// Positionen
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.PLAIN, 20));
		positions().forEach(p -> {
			Vector2 center = centerPoint(p);
			g.fillOval(center.roundedX() - posRadius, center.roundedY() - posRadius, 2 * posRadius, 2 * posRadius);
			if (showPositionNumbers) {
				g.drawString(p + "", center.x + 3 * posRadius, center.y + 3 * posRadius);
			}
		});

		// Steine
		stones().forEach(stone -> stone.draw(g));

		g.translate(-tf.getX(), -tf.getY());
	}
}