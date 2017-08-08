package de.amr.games.muehle.board;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The board data model with stone allocation and mill-related predicates.
 *
 * @author Armin Reichert, Peter & Anna Schillo
 */
public class BoardModel extends BoardGraph {

	protected static void checkStoneType(StoneColor type) {
		if (type == null) {
			throw new IllegalArgumentException("Illegal stone type: " + type);
		}
	}

	/*
	 * Auxiliary tables storing the horizontal and vertical mill partner positions.
	 */
	private static final int[][] H_MILL = {
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

	private static final int[][] V_MILL = {
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
	private StoneColor[] content;

	/**
	 * Constructs an empty board.
	 */
	public BoardModel() {
		content = new StoneColor[NUM_POS];
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @return a stream of the empty neighbor positions of p
	 */
	public IntStream emptyNeighbors(int p) {
		checkPosition(p);
		return neighbors(p).filter(this::isEmpty);
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @return if the position p has an empty neighbor position
	 */
	public boolean hasEmptyNeighbor(int p) {
		checkPosition(p);
		return neighbors(p).anyMatch(this::isEmpty);
	}

	/**
	 * Clears the board.
	 */
	public void clear() {
		content = new StoneColor[NUM_POS];
	}

	/**
	 * 
	 * @param type
	 *          a stone type
	 * @return a stream of the positions containing a stone of the given type
	 */
	public IntStream positions(StoneColor type) {
		checkStoneType(type);
		return positions().filter(p -> content[p] == type);
	}

	/**
	 * 
	 * @param type
	 *          a stone type
	 * @return a stream of the positions which carry a stone of the given type and which have an empty neighbor position
	 */
	public IntStream positionsWithEmptyNeighbor(StoneColor type) {
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
	public long stoneCount(StoneColor type) {
		checkStoneType(type);
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
	public void putStoneAt(int p, StoneColor type) {
		checkPosition(p);
		checkStoneType(type);
		if (content[p] != null) {
			throw new IllegalStateException("Position where stone is placed must be empty");
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
		checkPosition(p);
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
		checkPosition(from);
		checkPosition(to);
		if (content[from] == null) {
			throw new IllegalStateException("Position from where stone is moved must not be empty");
		}
		if (content[to] != null) {
			throw new IllegalStateException("Position where stone is moved to must be empty");
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
	public StoneColor getStoneAt(int p) {
		checkPosition(p);
		return content[p];
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @return if the position is empty
	 */
	public boolean isEmpty(int p) {
		checkPosition(p);
		return content[p] == null;
	}

	/**
	 * 
	 * @param p
	 *          a valid position
	 * @return if there is a stone at the position
	 */
	public boolean hasStoneAt(int p) {
		checkPosition(p);
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
	public boolean hasStoneAt(int p, StoneColor type) {
		checkPosition(p);
		checkStoneType(type);
		return content[p] == type;
	}

	// Stone movement:

	/**
	 * 
	 * @param type
	 *          a stone type
	 * @return if no stone of the given type can move to some neighbor position
	 */
	public boolean isTrapped(StoneColor type) {
		checkStoneType(type);
		return positions(type).noneMatch(p -> hasEmptyNeighbor(p));
	}

	// Mill related methods

	/**
	 * @param p
	 *          a valid position
	 * @param type
	 *          a stone type
	 * @return if the given position is inside a mill of stones of the given type
	 */
	public boolean isPositionInsideMill(int p, StoneColor type) {
		checkPosition(p);
		checkStoneType(type);
		return IntStream.of(p, H_MILL[p][0], H_MILL[p][1]).allMatch(q -> content[q] == type)
				|| IntStream.of(p, V_MILL[p][0], V_MILL[p][1]).allMatch(q -> content[q] == type);
	}

	/**
	 * @param type
	 *          a stone type
	 * @return if all stones of the given type are inside some mill
	 */
	public boolean areAllStonesInsideMill(StoneColor type) {
		return positions(type).allMatch(p -> isPositionInsideMill(p, type));
	}

	/**
	 * @param type
	 *          a stone type
	 * @return a stream of all positions which would close a mill when a stone of the given type would be placed there
	 */
	public IntStream positionsForClosingMill(StoneColor type) {
		return positions().filter(p -> canMillBeClosedAt(p, type));
	}

	/**
	 * @param p
	 *          a valid position
	 * @param type
	 *          a stone type
	 * @return if a mill of stones of the given type could be closed when placing a stone on the given position
	 */
	public boolean canMillBeClosedAt(int p, StoneColor type) {
		checkPosition(p);
		checkStoneType(type);
		return content[p] == null && (content[H_MILL[p][0]] == type && content[H_MILL[p][1]] == type
				|| content[V_MILL[p][0]] == type && content[V_MILL[p][1]] == type);
	}

	/**
	 * @param type
	 *          a stone type
	 * @return a stream of all positions where placing a stone of the given type would open two mills
	 */
	public IntStream positionsOpeningTwoMills(StoneColor type) {
		return positions().filter(p -> canOpenedTwoMillsAt(p, type));
	}

	/**
	 * @param p
	 *          a valid position
	 * @param type
	 *          a stone type
	 * @return if placing a stone of the given type at the given positions would open two mills
	 */
	public boolean canOpenedTwoMillsAt(int p, StoneColor type) {
		checkPosition(p);
		checkStoneType(type);
		if (content[p] != null) {
			return false;
		}
		int h1 = H_MILL[p][0], h2 = H_MILL[p][1];
		int v1 = V_MILL[p][0], v2 = V_MILL[p][1];
		return (content[h1] == type && content[h2] == null || content[h1] == null && content[h2] == type)
				&& (content[v1] == type && content[v2] == null || content[v1] == null && content[v2] == type);
	}
}