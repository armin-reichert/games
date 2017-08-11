package de.amr.games.muehle.board;

import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The board data model with stone allocation and mill-related predicates.
 *
 * @author Armin Reichert, Peter & Anna Schillo
 */
public class BoardModel extends BoardGraph {

	protected static void checkStoneColor(StoneColor color) {
		if (color == null) {
			throw new IllegalArgumentException("Illegal stone color: " + color);
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
	 * Clears the board.
	 */
	public void clear() {
		content = new StoneColor[NUM_POS];
	}

	/**
	 * 
	 * @param color
	 *          a stone color
	 * @return a stream of the positions with a stone of the given color
	 */
	public IntStream positions(StoneColor color) {
		checkStoneColor(color);
		return positions().filter(p -> content[p] == color);
	}

	/**
	 * @return the number of stones on the board
	 */
	public long stoneCount() {
		return Stream.of(content).filter(Objects::nonNull).count();
	}

	/**
	 * @param color
	 *          a stone color
	 * @return the number of stones with the given color
	 */
	public long stoneCount(StoneColor color) {
		checkStoneColor(color);
		return Stream.of(content).filter(c -> c == color).count();
	}

	/**
	 * Puts a stone with the given color at the given position. The position must not contain a stone already.
	 * 
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 */
	public void putStoneAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		if (content[p] != null) {
			throw new IllegalStateException("Position where stone is placed must be empty");
		}
		content[p] = color;
	}

	/**
	 * Removes a stone from the given position. Empty positions are allowed.
	 * 
	 * @param p
	 *          a valid position
	 */
	public void removeStoneAt(int p) {
		checkPosition(p);
		content[p] = null;
	}

	/**
	 * Moves the stone from position <code>from</code> to position <code>to</code>. If the source position is empty or the
	 * target position is not empty, an exception is thrown.
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
	 * @param p
	 *          a valid position
	 * @return the content at this position or <code>null</code>
	 */
	public StoneColor getStoneAt(int p) {
		checkPosition(p);
		return content[p];
	}

	/**
	 * @param p
	 *          a valid position
	 * @return if the position is empty
	 */
	public boolean isEmptyPosition(int p) {
		checkPosition(p);
		return content[p] == null;
	}

	/**
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
		checkStoneColor(type);
		return content[p] == type;
	}

	/**
	 * @param p
	 *          a valid position
	 * @return if the position p has an empty neighbor position
	 */
	public boolean hasEmptyNeighbor(int p) {
		checkPosition(p);
		return neighbors(p).anyMatch(this::isEmptyPosition);
	}

	/**
	 * @param p
	 *          a valid position
	 * @return a stream of the empty neighbor positions of p
	 */
	public IntStream emptyNeighbors(int p) {
		checkPosition(p);
		return neighbors(p).filter(this::isEmptyPosition);
	}

	/**
	 * @param color
	 *          a stone color
	 * @return a stream of the positions carrying a stone of the given color and having an empty neighbor position
	 */
	public IntStream positionsWithEmptyNeighbor(StoneColor color) {
		return positions(color).filter(this::hasEmptyNeighbor);
	}

	/**
	 * @param color
	 *          a stone color
	 * @return if no stone of the given color can move (jumping not possible)
	 */
	public boolean isTrapped(StoneColor color) {
		checkStoneColor(color);
		return positions(color).noneMatch(p -> hasEmptyNeighbor(p));
	}

	// Mill related methods

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if the given position is inside a horizontal mill of the given color
	 */
	public boolean isPositionInHorizontalMill(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return IntStream.of(p, H_MILL[p][0], H_MILL[p][1]).allMatch(q -> content[q] == color);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if the given position is inside a vertical mill of the given color
	 */
	public boolean isPositionInVerticalMill(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return IntStream.of(p, V_MILL[p][0], V_MILL[p][1]).allMatch(q -> content[q] == color);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if the given position is inside a mill of the given color
	 */
	public boolean isPositionInMill(int p, StoneColor color) {
		return isPositionInHorizontalMill(p, color) || isPositionInVerticalMill(p, color);
	}

	/**
	 * @param color
	 *          a stone color
	 * @return if all stones of the given color are inside some mill
	 */
	public boolean areAllStonesInMills(StoneColor color) {
		return positions(color).allMatch(p -> isPositionInMill(p, color));
	}

	/**
	 * @param color
	 *          a stone color
	 * @return a stream of all positions where a mill of the given color could be closed
	 */
	public IntStream positionsClosingMill(StoneColor color) {
		return positions().filter(p -> canMillBeClosedAt(p, color));
	}

	/**
	 * @param color
	 *          a stone color
	 * @return a stream of all positions where a mill of the given color could be opened
	 */
	public IntStream positionsOpeningMill(StoneColor color) {
		return positions().filter(p -> isMillOpenedAt(p, color));
	}

	/**
	 * @param color
	 *          a stone color
	 * @return a stream of all positions where two mills of the given color could be opened at once
	 */
	public IntStream positionsOpeningTwoMills(StoneColor color) {
		return positions().filter(p -> areTwoMillsOpenedAt(p, color));
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if placing a stone of the given color at the given position opens a horizontal mill
	 */
	public boolean isHorizontalMillOpenedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isXXXMillOpenedAt(p, color, H_MILL);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if placing a stone of the given color at the given position opens a vertical mill
	 */
	public boolean isVerticalMillOpenedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isXXXMillOpenedAt(p, color, V_MILL);
	}

	private boolean isXXXMillOpenedAt(int p, StoneColor color, int[][] mill) {
		int q = mill[p][0], r = mill[p][1];
		return content[p] == null
				&& (content[q] == color && content[r] == null || content[q] == color && content[r] == null);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if placing a stone of the given color at the given position would open a mill
	 */
	public boolean isMillOpenedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isHorizontalMillOpenedAt(p, color) || isVerticalMillOpenedAt(p, color);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if placing a stone of the given color at the given position would open two mills of that color
	 */
	public boolean areTwoMillsOpenedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isHorizontalMillOpenedAt(p, color) && isVerticalMillOpenedAt(p, color);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if a mill of the given color would be closed by placing a stone of that color at the given position
	 */
	public boolean canMillBeClosedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return content[p] == null && (content[H_MILL[p][0]] == color && content[H_MILL[p][1]] == color
				|| content[V_MILL[p][0]] == color && content[V_MILL[p][1]] == color);
	}

	/**
	 * @param from
	 *          move start position
	 * @param to
	 *          move end position
	 * @param color
	 *          stone color
	 * @return if a mill of the given color is closed when moving a stone of the given color from the start to the end
	 *         position
	 */
	public boolean isMillClosedWhenMoving(int from, int to, StoneColor color) {
		checkPosition(from);
		checkPosition(to);
		checkStoneColor(color);
		if (hasStoneAt(from, color) && isEmptyPosition(to)) {
			Optional<Direction> optDir = getDirection(from, to);
			if (optDir.isPresent()) {
				Direction dir = optDir.get();
				if (dir == NORTH || dir == SOUTH) {
					return getStoneAt(H_MILL[to][0]) == color && getStoneAt(H_MILL[to][1]) == color;
				} else {
					return getStoneAt(V_MILL[to][0]) == color && getStoneAt(V_MILL[to][1]) == color;
				}
			}
		}
		return false;
	}

	/**
	 * @param color
	 *          stone color
	 * @return positions where by placing a stone two mills of the same color could be opened later
	 */
	public IntStream positionsOpeningTwoMillsLater(StoneColor color) {
		return positions().filter(this::isEmptyPosition).filter(p -> hasTwoMillsLaterPartnerPosition(p, color));
	}

	/**
	 * @param p
	 *          valid position
	 * @param color
	 *          stone color
	 * @return if by placing a stone of the given color at the position later two mills could be opened
	 */
	public boolean hasTwoMillsLaterPartnerPosition(int p, StoneColor color) {
		return distance2Positions(p).filter(q -> getStoneAt(q) == color)
				.anyMatch(q -> areTwoMillsPossibleLater(p, q, color));
	}

	/**
	 * @param p
	 *          valid position
	 * @return stream of all positions which have distance 2 from given position
	 */
	public IntStream distance2Positions(int p) {
		checkPosition(p);
		return neighbors(p).flatMap(this::neighbors).distinct().filter(q -> q != p);
	}

	private boolean areTwoMillsPossibleLater(int p, int q, StoneColor color) {
		int commonNeighbor = neighbors(p).filter(n -> areNeighbors(n, q)).findFirst().getAsInt();
		Direction dir1 = getDirection(p, commonNeighbor).get();
		OptionalInt otherNeighbor1 = neighbor(p, dir1.opposite());
		if (!otherNeighbor1.isPresent()) {
			otherNeighbor1 = neighbor(commonNeighbor, dir1);
		}
		Direction dir2 = getDirection(q, commonNeighbor).get();
		OptionalInt otherNeighbor2 = neighbor(q, dir2.opposite());
		if (!otherNeighbor2.isPresent()) {
			otherNeighbor2 = neighbor(commonNeighbor, dir2);
		}
		return otherNeighbor1.isPresent() && isEmptyPosition(otherNeighbor1.getAsInt()) && otherNeighbor2.isPresent()
				&& isEmptyPosition(otherNeighbor2.getAsInt());
	}
}