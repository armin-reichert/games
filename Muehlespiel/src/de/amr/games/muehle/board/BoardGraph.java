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

	/**
	 * Constructs an empty board.
	 */
	public BoardGraph() {
		content = new StoneType[NUM_POS];
	}

	// Board topology:

	/**
	 * @return a stream of the board positions
	 */
	public IntStream positions() {
		return IntStream.range(0, NUM_POS);
	}

	/**
	 * @param p
	 *          a valid position
	 * @return a stream of the neighbor positions
	 */
	public IntStream neighbors(int p) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		return IntStream.of(NEIGHBORS[p]).filter(n -> n != -1);
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @param dir
	 *          a valid direction
	 * @return the neighbor in the given position or <code>-1</code>
	 */
	public int neighbor(int p, Direction dir) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		Objects.requireNonNull(dir);
		return NEIGHBORS[p][dir.ordinal()];
	}

	private int neighbor(int p, Direction dir, StoneType type) {
		int n = neighbor(p, dir);
		return n != -1 && hasStoneAt(n) && getStoneAt(n) == type ? n : -1;
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @return a stream of the empty neighbor positions of p
	 */
	public IntStream emptyNeighbors(int p) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		return neighbors(p).filter(this::isEmpty);
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @return if the position p has an empy neighbor position
	 */
	public boolean hasEmptyNeighbor(int p) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		return neighbors(p).anyMatch(this::isEmpty);
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @param q
	 *          a valid position
	 * @return if the given positions are neighbors
	 */
	public boolean areNeighbors(int p, int q) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		if (q == -1) {
			throw new IllegalArgumentException();
		}
		return neighbors(p).anyMatch(n -> n == q);
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @param q
	 *          a valid position
	 * @return the direction from <code>p</code> to <code>q</code> if <code>p</code> and <code>q</code> are neighbors,
	 *         otherwise <code>-1</code>
	 */
	public Direction getDirection(int p, int q) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		if (q == -1) {
			throw new IllegalArgumentException();
		}
		return Stream.of(Direction.values()).filter(dir -> neighbor(p, dir) == q).findFirst().orElse(null);
	}

	// Stone assignment related methods:

	/**
	 * Clears the board.
	 */
	public void clear() {
		content = new StoneType[NUM_POS];
	}

	/**
	 * 
	 * @param type
	 *          a stone type
	 * @return a stream of the positions containing a stone of the given type
	 */
	public IntStream positions(StoneType type) {
		Objects.requireNonNull(type);
		return positions().filter(p -> content[p] == type);
	}

	/**
	 * 
	 * @param type
	 *          a stone type
	 * @return a stream of the positions which carry a stone of the given type and which have an empty neighbor position
	 */
	public IntStream positionsWithEmptyNeighbor(StoneType type) {
		Objects.requireNonNull(type);
		return positions(type).filter(this::hasEmptyNeighbor);
	}

	/**
	 * @return the number of stones on the board
	 */
	public long stoneCount() {
		return Stream.of(content).filter(Objects::nonNull).count();
	}

	/**
	 * 
	 * @param type
	 *          a stone type
	 * @return the number of stones of the given type on the board
	 */
	public long stoneCount(StoneType type) {
		Objects.requireNonNull(type);
		return Stream.of(content).filter(stone -> stone == type).count();
	}

	/**
	 * Puts a stone of the given type to the given position. The position may not contain a stone already.
	 * 
	 * @param p
	 *          a valid position
	 * @param type
	 *          a stone type
	 */
	public void putStoneAt(int p, StoneType type) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		Objects.requireNonNull(type);
		if (content[p] != null) {
			throw new IllegalStateException("Zielposition muss leer sein");
		}
		content[p] = type;
	}

	/**
	 * Removes a stone from the given position. If the position is empty, nothing is done.
	 * 
	 * @param p
	 *          a valid position
	 */
	public void removeStoneAt(int p) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		content[p] = null;
	}

	/**
	 * Moves the stone at position <code>from</code> to position <code>to</code>. If either the source position is empty
	 * or the target position is not empty, an exception is thrown.
	 * 
	 * @param from
	 *          the source position
	 * @param to
	 *          the target position
	 */
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

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @return the content at this position or <code>null</code>
	 */
	public StoneType getStoneAt(int p) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		return content[p];
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @return if the position is empty
	 */
	public boolean isEmpty(int p) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		return content[p] == null;
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @return if there is a stone at the position
	 */
	public boolean hasStoneAt(int p) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		return content[p] != null;
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @param type
	 *          a stone type
	 * @return if there is a stone of the given type at this position
	 */
	public boolean hasStoneAt(int p, StoneType type) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		Objects.requireNonNull(type);
		return content[p] == type;
	}

	// Stone movement:

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @return if there is a stone at this position and this stone can move to some neighbor position
	 */
	public boolean canMoveStoneFrom(int p) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		return hasStoneAt(p) && hasEmptyNeighbor(p);
	}

	/**
	 * 
	 * @param type
	 *          a stone type
	 * @return if no stone of the given type can move to some neighbor position
	 */
	public boolean cannotMoveStones(StoneType type) {
		Objects.requireNonNull(type);
		return positions(type).allMatch(p -> emptyNeighbors(p).count() == 0);
	}

	// Mill related methods

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @param type
	 *          a stone type
	 * @return if the given position is inside a mill of stones of the given type
	 */
	public boolean isPositionInsideMill(int p, StoneType type) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		Objects.requireNonNull(type);
		return findContainingMill(p, type, true) != null || findContainingMill(p, type, false) != null;
	}

	/**
	 * 
	 * @param type
	 *          a stone type
	 * @return if all stones of the given type are inside some mill
	 */
	public boolean areAllStonesInsideMill(StoneType type) {
		Objects.requireNonNull(type);
		return positions(type).allMatch(p -> isPositionInsideMill(p, type));
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @param type
	 *          a stone type
	 * @param horizontal
	 * @return the horizontal mill (if existing) of stones of the given type containing the given position, or
	 *         <code>null</code> if there is no such mill
	 */
	public Mill findContainingMill(int p, StoneType type, boolean horizontal) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		Objects.requireNonNull(type);

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

	/**
	 * 
	 * @param type
	 *          a stone type
	 * @return a stream of all positions which would close a mill when a stone of the given type would be placed there
	 */
	public IntStream positionsForClosingMill(StoneType type) {
		Objects.requireNonNull(type);
		return positions().filter(p -> canMillBeClosedAt(p, type));
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @param type
	 *          a stone type
	 * @return if a mill of stones of the given type could be closed when placing a stone on the given position
	 */
	public boolean canMillBeClosedAt(int p, StoneType type) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		Objects.requireNonNull(type);

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

	/**
	 * 
	 * @param type
	 *          a stone type
	 * @return a stream of all positions where placing a stone of the given type would open two mills
	 */
	public IntStream positionsForOpeningTwoMills(StoneType type) {
		Objects.requireNonNull(type);
		return positions().filter(p -> canTwoMillsBeOpenedAt(p, type));
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @param type
	 *          a stone type
	 * @return if placing a stone of the given type at the given positions would open two mills
	 */
	public boolean canTwoMillsBeOpenedAt(int p, StoneType type) {
		if (p == -1) {
			throw new IllegalArgumentException();
		}
		Objects.requireNonNull(type);

		if (hasStoneAt(p)) {
			return false;
		}
		int[] row = POSSIBLE_MILLS[p];
		int h1 = row[0], h2 = row[1], v1 = row[2], v2 = row[3];
		return (hasStoneAt(h1, type) && isEmpty(h2) || isEmpty(h1) && hasStoneAt(h2, type))
				&& (hasStoneAt(v1, type) && isEmpty(v2) || isEmpty(v1) && hasStoneAt(v2, type));
	}
}