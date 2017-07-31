package de.amr.games.muehle.board;

import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The board data model.
 *
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class Board {

	public static final int NUM_POS = 24;

	/*
	 * An adjacency list-like representation of the board graph.
	 * 
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
	 * Auxiliary table storing the horizontal and vertical mill partner positions.
	 */
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

	private StoneColor[] colors;

	public Board() {
		colors = new StoneColor[NUM_POS];
	}

	// Board topology:

	public IntStream positions() {
		return IntStream.range(0, NUM_POS);
	}

	public IntStream neighbors(int p) {
		return IntStream.of(NEIGHBORS[p]).filter(n -> n != -1);
	}

	public int neighbor(int p, Direction dir) {
		return NEIGHBORS[p][dir.ordinal()];
	}

	private int neighbor(int p, Direction dir, StoneColor color) {
		int n = neighbor(p, dir);
		return n != -1 && hasStoneAt(n) && getStoneAt(n) == color ? n : -1;
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

	public Direction getDirection(int p, int q) {
		return Stream.of(Direction.values()).filter(dir -> neighbor(p, dir) == q).findFirst().orElse(null);
	}

	// Stone assignment related methods:

	public void clear() {
		colors = new StoneColor[NUM_POS];
	}

	public IntStream positions(StoneColor color) {
		return positions().filter(p -> colors[p] == color);
	}

	public IntStream positionsWithEmptyNeighbor(StoneColor color) {
		return positions(color).filter(this::hasEmptyNeighbor);
	}

	public Stream<StoneColor> stones() {
		return Stream.of(colors).filter(Objects::nonNull);
	}

	public Stream<StoneColor> stones(StoneColor color) {
		return stones().filter(stone -> stone == color);
	}

	public void putStoneAt(int p, StoneColor color) {
		if (colors[p] != null) {
			throw new IllegalStateException("Zielposition muss leer sein");
		}
		colors[p] = color;
	}

	public void removeStoneAt(int p) {
		colors[p] = null;
	}

	public void moveStone(int from, int to) {
		if (colors[from] == null) {
			throw new IllegalStateException("Startposition muss einen Stein enthalten");
		}
		if (colors[to] != null) {
			throw new IllegalStateException("Zielposition muss leer sein");
		}
		colors[to] = colors[from];
		colors[from] = null;
	}

	public StoneColor getStoneAt(int p) {
		return colors[p];
	}

	public boolean isEmpty(int p) {
		return colors[p] == null;
	}

	public boolean hasStoneAt(int p) {
		return colors[p] != null;
	}

	public boolean hasStoneAt(int p, StoneColor color) {
		return colors[p] == color;
	}

	// Stone movement:

	public boolean canMoveStoneFrom(int p) {
		return hasStoneAt(p) && hasEmptyNeighbor(p);
	}

	public boolean cannotMoveStones(StoneColor color) {
		return positions(color).allMatch(p -> emptyNeighbors(p).count() == 0);
	}

	// Mill related methods

	public boolean isPositionInsideMill(int p, StoneColor color) {
		return findContainingMill(p, color, true) != null || findContainingMill(p, color, false) != null;
	}

	public boolean areAllStonesInsideMill(StoneColor color) {
		return positions(color).allMatch(p -> isPositionInsideMill(p, color));
	}

	public Mill findContainingMill(int p, StoneColor color, boolean horizontal) {
		StoneColor stone = getStoneAt(p);
		if (stone == null || stone != color) {
			return null;
		}

		Direction left = horizontal ? WEST : NORTH;
		Direction right = horizontal ? EAST : SOUTH;

		int q, r;

		// p -> q -> r
		q = neighbor(p, right, color);
		if (q != -1) {
			r = neighbor(q, right, color);
			if (r != -1) {
				return new Mill(p, q, r, horizontal);
			}
		}

		// q <- p -> r
		q = neighbor(p, left, color);
		if (q != -1) {
			r = neighbor(p, right, color);
			if (r != -1) {
				return new Mill(q, p, r, horizontal);
			}
		}

		// q <- r <- p
		r = neighbor(p, left, color);
		if (r != -1) {
			q = neighbor(r, left, color);
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
		if (hasStoneAt(h1, color) && hasStoneAt(h2, color)) {
			return true;
		}
		int v1 = row[2], v2 = row[3];
		if (hasStoneAt(v1, color) && hasStoneAt(v2, color)) {
			return true;
		}
		return false;
	}

	public IntStream positionsForOpeningTwoMills(StoneColor color) {
		return positions().filter(p -> canTwoMillsBeOpenedAt(p, color));
	}

	public boolean canTwoMillsBeOpenedAt(int p, StoneColor color) {
		if (hasStoneAt(p)) {
			return false;
		}
		int[] row = POSSIBLE_MILLS[p];
		int h1 = row[0], h2 = row[1], v1 = row[2], v2 = row[3];
		return (hasStoneAt(h1, color) && isEmpty(h2) || isEmpty(h1) && hasStoneAt(h2, color))
				&& (hasStoneAt(v1, color) && isEmpty(v2) || isEmpty(v1) && hasStoneAt(v2, color));
	}

}