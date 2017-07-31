package de.amr.games.muehle.board;

import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The board data model as an undirected oriented graph. Each node has at most one neighbor in one of the four
 * directions. Nodes are numbered row-wise top to bottom.
 *
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class BoardGraph {

	/** The number of positions (nodes). */
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

	/* Stone content */
	private StoneType[] content;

	public BoardGraph() {
		content = new StoneType[NUM_POS];
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

	private int neighbor(int p, Direction dir, StoneType type) {
		int n = neighbor(p, dir);
		return n != -1 && hasStoneAt(n) && getStoneAt(n) == type ? n : -1;
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
		content = new StoneType[NUM_POS];
	}

	public IntStream positions(StoneType type) {
		return positions().filter(p -> content[p] == type);
	}

	public IntStream positionsWithEmptyNeighbor(StoneType type) {
		return positions(type).filter(this::hasEmptyNeighbor);
	}

	public Stream<StoneType> stones() {
		return Stream.of(content).filter(Objects::nonNull);
	}

	public Stream<StoneType> stones(StoneType type) {
		return stones().filter(stone -> stone == type);
	}

	public void putStoneAt(int p, StoneType type) {
		if (content[p] != null) {
			throw new IllegalStateException("Zielposition muss leer sein");
		}
		content[p] = type;
	}

	public void removeStoneAt(int p) {
		content[p] = null;
	}

	public void moveStone(int from, int to) {
		if (content[from] == null) {
			throw new IllegalStateException("Startposition muss einen Stein enthalten");
		}
		if (content[to] != null) {
			throw new IllegalStateException("Zielposition muss leer sein");
		}
		content[to] = content[from];
		content[from] = null;
	}

	public StoneType getStoneAt(int p) {
		return content[p];
	}

	public boolean isEmpty(int p) {
		return content[p] == null;
	}

	public boolean hasStoneAt(int p) {
		return content[p] != null;
	}

	public boolean hasStoneAt(int p, StoneType type) {
		return content[p] == type;
	}

	// Stone movement:

	public boolean canMoveStoneFrom(int p) {
		return hasStoneAt(p) && hasEmptyNeighbor(p);
	}

	public boolean cannotMoveStones(StoneType type) {
		return positions(type).allMatch(p -> emptyNeighbors(p).count() == 0);
	}

	// Mill related methods

	public boolean isPositionInsideMill(int p, StoneType type) {
		return findContainingMill(p, type, true) != null || findContainingMill(p, type, false) != null;
	}

	public boolean areAllStonesInsideMill(StoneType type) {
		return positions(type).allMatch(p -> isPositionInsideMill(p, type));
	}

	public Mill findContainingMill(int p, StoneType type, boolean horizontal) {
		StoneType stone = getStoneAt(p);
		if (stone == null || stone != type) {
			return null;
		}

		Direction left = horizontal ? WEST : NORTH;
		Direction right = horizontal ? EAST : SOUTH;

		int q, r;

		// p -> q -> r
		q = neighbor(p, right, type);
		if (q != -1) {
			r = neighbor(q, right, type);
			if (r != -1) {
				return new Mill(p, q, r, horizontal);
			}
		}

		// q <- p -> r
		q = neighbor(p, left, type);
		if (q != -1) {
			r = neighbor(p, right, type);
			if (r != -1) {
				return new Mill(q, p, r, horizontal);
			}
		}

		// q <- r <- p
		r = neighbor(p, left, type);
		if (r != -1) {
			q = neighbor(r, left, type);
			if (q != -1) {
				return new Mill(q, r, p, horizontal);
			}
		}

		return null;
	}

	public IntStream positionsForClosingMill(StoneType type) {
		return positions().filter(p -> canMillBeClosedAt(p, type));
	}

	public boolean canMillBeClosedAt(int p, StoneType type) {
		if (hasStoneAt(p)) {
			return false;
		}
		int[] row = POSSIBLE_MILLS[p];
		int h1 = row[0], h2 = row[1];
		if (hasStoneAt(h1, type) && hasStoneAt(h2, type)) {
			return true;
		}
		int v1 = row[2], v2 = row[3];
		if (hasStoneAt(v1, type) && hasStoneAt(v2, type)) {
			return true;
		}
		return false;
	}

	public IntStream positionsForOpeningTwoMills(StoneType type) {
		return positions().filter(p -> canTwoMillsBeOpenedAt(p, type));
	}

	public boolean canTwoMillsBeOpenedAt(int p, StoneType type) {
		if (hasStoneAt(p)) {
			return false;
		}
		int[] row = POSSIBLE_MILLS[p];
		int h1 = row[0], h2 = row[1], v1 = row[2], v2 = row[3];
		return (hasStoneAt(h1, type) && isEmpty(h2) || isEmpty(h1) && hasStoneAt(h2, type))
				&& (hasStoneAt(v1, type) && isEmpty(v2) || isEmpty(v1) && hasStoneAt(v2, type));
	}

}