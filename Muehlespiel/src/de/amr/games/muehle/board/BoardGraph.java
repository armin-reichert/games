package de.amr.games.muehle.board;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The board as an undirected oriented graph. Each board position (node) has at most one neighbor in
 * one of the four directions. Nodes are numbered row-wise top to bottom.
 *
 * @author Armin Reichert, Peter & Anna Schillo
 */
public class BoardGraph {

	/** The number of positions (nodes). */
	public static final int NUM_POS = 24;

	/*
	 * An adjacency list-like representation of the board graph.
	 * 
	 * NEIGHBORS[p] = {neighbor(p, NORTH), neighbor(p, EAST), neighbor(p, SOUTH), neighbor(p, WEST)}
	 * -1 = no neighbor
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

	/*
	 * Horizontal mill partner positions.
	 */
	protected static final int[][] HMILL = {
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

	/*
	 * Vertical mill partner positions.
	 */
	protected static final int[][] VMILL = {
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
	 * 
	 * @param p
	 *          a number
	 * @return if the given number denotes a valid board position
	 */
	public boolean isValidPosition(int p) {
		return 0 <= p && p < NUM_POS;
	}

	/**
	 * @param p
	 *          a position
	 * @return a stream of the neighbor positions
	 */
	public IntStream neighbors(int p) {
		checkPosition(p);
		return IntStream.of(NEIGHBORS[p]).filter(q -> q != -1);
	}

	/**
	 * @param p
	 *          a position
	 * @param dir
	 *          a direction
	 * @return the (optional) neighbor in the given direction
	 */
	public OptionalInt neighbor(int p, Direction dir) {
		checkPosition(p);
		checkDirection(dir);
		int q = NEIGHBORS[p][dir.ordinal()];
		return q != -1 ? OptionalInt.of(q) : OptionalInt.empty();
	}

	/**
	 * @param p
	 *          a position
	 * @return stream of all positions which have distance 2 from given position
	 */
	public IntStream nextToNeighbors(int p) {
		checkPosition(p);
		return neighbors(p).flatMap(this::neighbors).distinct().filter(q -> q != p);
	}

	/**
	 * @param p
	 *          a position
	 * @param q
	 *          a position
	 * @return if the given positions are neighbors
	 */
	public boolean areNeighbors(int p, int q) {
		checkPosition(p);
		checkPosition(q);
		return neighbors(p).anyMatch(n -> n == q);
	}

	/**
	 * @param p
	 *          a position
	 * @param q
	 *          a position
	 * @return the (optional) direction from <code>p</code> to <code>q</code> if <code>p</code> and
	 *         <code>q</code> are neighbors
	 */
	public Optional<Direction> getDirection(int p, int q) {
		checkPosition(p);
		checkPosition(q);
		return Stream.of(Direction.values()).filter(dir -> NEIGHBORS[p][dir.ordinal()] == q)
				.findFirst();
	}

	/**
	 * @param p
	 *          a position
	 * @param q
	 *          a position
	 * @param r
	 *          a position
	 * @return if the given positions form a horizontal mill
	 */
	public boolean areHMillPositions(int p, int q, int r) {
		checkPosition(p);
		checkPosition(q);
		checkPosition(r);
		int h1 = HMILL[p][0], h2 = HMILL[p][1];
		return h1 == q && h2 == r || h1 == r && h2 == q;
	}

	/**
	 * @param p
	 *          a position
	 * @param q
	 *          a position
	 * @param r
	 *          a position
	 * @return if the given positions form a vertical mill
	 */
	public boolean areVMillPositions(int p, int q, int r) {
		checkPosition(p);
		checkPosition(q);
		checkPosition(r);
		int v1 = VMILL[p][0], v2 = VMILL[p][1];
		return v1 == q && v2 == r || v1 == r && v2 == q;
	}
}