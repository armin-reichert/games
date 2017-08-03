package de.amr.games.muehle.board;

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
	 * Auxiliary tables storing the horizontal and vertical mill partner positions.
	 */
	private static final int[][] H_MILL_PARTNERS = {
			/*@formatter:off*/
			{ 1, 2 },	
			{ 0, 2 },
			{ 0, 1 },
			{ 4, 5 },
			{ 3, 5 },
			{ 3, 4 },
			{ 7, 8 },
			{ 6, 8 },
			{ 6, 7 },
			{ 10, 11 },
			{ 9, 11 },
			{ 9, 10 },
			{ 13, 14 },
			{ 12, 14 },
			{ 12, 13 },
			{ 16, 17 }, 
			{ 15, 17 },
			{ 15, 16 },
			{ 19, 20 },
			{ 18, 20 },
			{ 18, 19 },
			{ 22, 23 },
			{ 21, 23 },
			{ 21, 22 }
			/*@formatter:on*/
	};

	private static final int[][] V_MILL_PARTNERS = {
			/*@formatter:off*/
			{ 9, 21 },	
			{ 4, 7 },
			{ 14, 23 },
			{ 10, 18 },
			{ 1, 7 },
			{ 13, 20 },
			{ 11, 15 },
			{ 1, 4 },
			{ 12, 17 },
			{ 0, 21 },
			{ 3, 18 },
			{ 6, 15 },
			{ 8, 17 },
			{ 5, 20 },
			{ 2, 23 },
			{ 6, 11 }, 
			{ 19, 22 },
			{ 8, 12 },
			{ 3, 10 },
			{ 16, 22 },
			{ 5, 13 },
			{ 0, 9 },
			{ 16, 19 },
			{ 2, 14 }
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

		return Stream.of(p, H_MILL_PARTNERS[p][0], H_MILL_PARTNERS[p][1]).allMatch(q -> getStoneAt(q) == type)
				|| Stream.of(p, V_MILL_PARTNERS[p][0], V_MILL_PARTNERS[p][1]).allMatch(q -> getStoneAt(q) == type);
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
		return getStoneAt(H_MILL_PARTNERS[p][0]) == type && getStoneAt(H_MILL_PARTNERS[p][1]) == type
				|| getStoneAt(V_MILL_PARTNERS[p][0]) == type && getStoneAt(V_MILL_PARTNERS[p][1]) == type;
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
		int h1 = H_MILL_PARTNERS[p][0], h2 = H_MILL_PARTNERS[p][1];
		int v1 = V_MILL_PARTNERS[p][0], v2 = V_MILL_PARTNERS[p][1];
		return (hasStoneAt(h1, type) && isEmpty(h2) || isEmpty(h1) && hasStoneAt(h2, type))
				&& (hasStoneAt(v1, type) && isEmpty(v2) || isEmpty(v1) && hasStoneAt(v2, type));
	}
}