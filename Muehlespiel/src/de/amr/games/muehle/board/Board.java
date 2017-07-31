package de.amr.games.muehle.board;

import static de.amr.easy.game.math.Vector2.dist;
import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;
import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;

/**
 * The board.
 *
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class Board extends GameEntity {

	public static final int NUM_POS = 24;

	/*
	 * NEIGHBORS[p] = { neighbor(North), neighbor(East), neighbor(South), neighbor(West) }, -1 = no neighbor
	 */
	private static final int[][] NEIGHBORS = {
		/*@formatter:off*/
		{ -1, 1, 9, -1 }, 
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
	 * (GRID_X[p], GRID_Y[p]) is the grid coordinate of position p in the board's [0..6] x [0..6] grid.
	 */
	private static final int[] GRID_X = { 0, 3, 6, 1, 3, 5, 2, 3, 4, 0, 1, 2, 4, 5, 6, 2, 3, 4, 1, 3, 5, 0, 3, 6 };
	private static final int[] GRID_Y = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6 };

	private static final int[][] POSSIBLE_MILLS = {
			/*@formatter:off*/
			/* h1, h2, v1, v2 */
			{ 1, 2, 9, 21 },	
			{ 0, 2, 4, 7 },
			{ 0, 1, 14, 23 },
			{ 4, 5, 10, 18 },
			{ 3, 5, 1, 7 },
			{ 3, 4, 13, 20 },
			{ 7, 8, 11, 15 },
			{ 6, 8, 1, 4 },
			{ 6, 7, 12, 17 },
			{ 10, 11, 0, 21 },
			{ 9, 11, 3, 18 },
			{ 9, 10, 6, 15 },
			{ 13, 14, 8, 17 },
			{ 12, 14, 5, 20 },
			{ 12, 13, 2, 23 },
			{ 16, 17, 6, 11 }, 
			{ 15, 17, 19, 22 },
			{ 15, 16, 8, 12 },
			{ 19, 20, 3, 10 },
			{ 18, 20, 16, 22 },
			{ 18, 19, 5, 13 },
			{ 22, 23, 0, 9 },
			{ 21, 23, 16, 19 },
			{ 21, 22, 2, 14 }
			/*@formatter:on*/
	};

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

	// Stone assignment:

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

	public boolean containsStoneOfColor(int p, StoneColor color) {
		return hasStoneAt(p) && getStoneAt(p).getColor() == color;
	}

	public IntStream positionsWithStone(StoneColor color) {
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

	public Set<Integer> freeNeighbors(StoneColor color) {
		Set<Integer> result = new HashSet<>();
		positionsWithStone(color).forEach(p -> {
			emptyNeighbors(p).forEach(n -> {
				result.add(n);
			});
		});
		return result;
	}

	// Board topology:

	public IntStream positions() {
		return IntStream.range(0, NUM_POS);
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

	public IntStream emptyNeighbors(int p) {
		return neighbors(p).filter(this::isEmpty);
	}

	public boolean hasEmptyNeighbor(int p) {
		return neighbors(p).anyMatch(this::isEmpty);
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

	// Stone movement:

	public boolean canMoveStoneFrom(int p) {
		return hasStoneAt(p) && hasEmptyNeighbor(p);
	}

	public boolean cannotMoveStones(StoneColor color) {
		return positionsWithStone(color).allMatch(p -> emptyNeighbors(p).count() == 0);
	}

	public IntStream allMovableStonePositions(StoneColor color) {
		return positionsWithStone(color).filter(this::hasEmptyNeighbor);
	}

	public int findNearestPosition(int x, int y, int radius) {
		return positions().filter(p -> dist(centerPoint(p), new Vector2(x, y)) <= radius).findFirst().orElse(-1);
	}

	// Mill related methods

	public boolean isPositionInsideMill(int p, StoneColor color) {
		return findContainingMill(p, color, true) != null || findContainingMill(p, color, false) != null;
	}

	public boolean areAllStonesInsideMill(StoneColor color) {
		return positionsWithStone(color).allMatch(p -> isPositionInsideMill(p, color));
	}

	public Mill findContainingMill(int p, StoneColor color, boolean horizontal) {
		Stone stone = getStoneAt(p);
		if (stone == null || stone.getColor() != color) {
			return null;
		}

		Direction left = horizontal ? WEST : NORTH;
		Direction right = horizontal ? EAST : SOUTH;

		int q, r;

		// p -> q -> r
		q = findNeighbor(p, right, color);
		if (q != -1) {
			r = findNeighbor(q, right, color);
			if (r != -1) {
				return new Mill(p, q, r, horizontal);
			}
		}

		// q <- p -> r
		q = findNeighbor(p, left, color);
		if (q != -1) {
			r = findNeighbor(p, right, color);
			if (r != -1) {
				return new Mill(q, p, r, horizontal);
			}
		}

		// q <- r <- p
		r = findNeighbor(p, left, color);
		if (r != -1) {
			q = findNeighbor(r, left, color);
			if (q != -1) {
				return new Mill(q, r, p, horizontal);
			}
		}

		return null;
	}

	public IntStream positionsForClosingMill(StoneColor color) {
		return positions().filter(p -> canMillBeClosedAt(p, color));
	}

	public boolean canMillBeClosedAt(int p, StoneColor color) {
		if (hasStoneAt(p)) {
			return false;
		}
		int[] row = POSSIBLE_MILLS[p];
		int h1 = row[0], h2 = row[1];
		if (containsStoneOfColor(h1, color) && containsStoneOfColor(h2, color)) {
			return true;
		}
		int v1 = row[2], v2 = row[3];
		if (containsStoneOfColor(v1, color) && containsStoneOfColor(v2, color)) {
			return true;
		}
		return false;
	}

	public IntStream positionsForOpening2Mills(StoneColor color) {
		return positions().filter(p -> can2MillsBeOpenedAt(p, color));
	}

	public boolean can2MillsBeOpenedAt(int p, StoneColor color) {
		if (hasStoneAt(p)) {
			return false;
		}
		int[] row = POSSIBLE_MILLS[p];
		int h1 = row[0], h2 = row[1], v1 = row[2], v2 = row[3];
		return (containsStoneOfColor(h1, color) && isEmpty(h2) || isEmpty(h1) && containsStoneOfColor(h2, color))
				&& (containsStoneOfColor(v1, color) && isEmpty(v2) || isEmpty(v1) && containsStoneOfColor(v2, color));
	}

	// Drawing related methods

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