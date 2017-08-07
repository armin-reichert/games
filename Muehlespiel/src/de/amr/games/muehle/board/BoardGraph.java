package de.amr.games.muehle.board;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The board data model as an undirected oriented graph. Each node has at most one neighbor in one of the four
 * directions. Nodes are numbered row-wise top to bottom.
 *
 * @author Armin Reichert, Peter & Anna Schillo
 */
public class BoardGraph {

	/** The number of positions (nodes). */
	public static final int NUM_POS = 24;

	/*
	 * An adjacency list-like representation of the board graph.
	 * 
	 * NEIGHBORS[p] = { neighbor(North), neighbor(East), neighbor(South), neighbor(West) }, -1 = no neighbor
	 */
	protected static final int[][] NEIGHBORS = {
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

	protected static void checkPosition(int p) {
		if (p < 0 || p >= NUM_POS) {
			throw new IllegalArgumentException("Illegal position: " + p);
		}
	}

	protected static void checkDirection(Direction dir) {
		if (dir == null) {
			throw new IllegalArgumentException("Illegal direction: " + dir);
		}
	}

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
		checkPosition(p);
		return IntStream.of(NEIGHBORS[p]).filter(q -> q != -1);
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @param dir
	 *          a valid direction
	 * @return the (optional) neighbor in the given direction
	 */
	public OptionalInt neighbor(int p, Direction dir) {
		checkPosition(p);
		checkDirection(dir);
		int q = NEIGHBORS[p][dir.ordinal()];
		return q != -1 ? OptionalInt.of(q) : OptionalInt.empty();
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
		checkPosition(p);
		checkPosition(q);
		return neighbors(p).anyMatch(n -> n == q);
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @param q
	 *          a valid position
	 * @return the (optional) direction from <code>p</code> to <code>q</code> if <code>p</code> and <code>q</code> are
	 *         neighbors
	 */
	public Optional<Direction> getDirection(int p, int q) {
		checkPosition(p);
		checkPosition(q);
		return Stream.of(Direction.values()).filter(dir -> NEIGHBORS[p][dir.ordinal()] == q).findFirst();
	}
}