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
public class Board extends BoardGraph {

	protected static void checkStoneColor(StoneColor color) {
		if (color == null) {
			throw new IllegalArgumentException("Illegal stone color: " + color);
		}
	}

	private final StoneColor[] content;

	/**
	 * Constructs an empty board.
	 */
	public Board() {
		content = new StoneColor[NUM_POS];
	}

	/**
	 * Clears the board.
	 */
	public void clear() {
		positions().forEach(p -> content[p] = null);
	}

	/**
	 * @param color
	 *          a stone color
	 * @return a stream of the positions with a stone of the given color
	 */
	public IntStream positions(StoneColor color) {
		checkStoneColor(color);
		return positions().filter(p -> content[p] == color);
	}

	/**
	 * @return the number of stones
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
		return positions(color).noneMatch(this::hasEmptyNeighbor);
	}

	// Mill related methods

	/**
	 * @param p
	 *          a valid position
	 * @param q
	 *          a valid position
	 * @param r
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if the given positions form a horizontal mill of the given color
	 */
	public boolean hasHMill(int p, int q, int r, StoneColor color) {
		checkPosition(p);
		checkPosition(q);
		checkPosition(r);
		checkStoneColor(color);
		return areHMillPositions(p, q, r) && IntStream.of(p, q, r).allMatch(pos -> content[pos] == color);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param q
	 *          a valid position
	 * @param r
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if the given positions form a vertical mill of the given color
	 */
	public boolean hasVMill(int p, int q, int r, StoneColor color) {
		checkPosition(p);
		checkPosition(q);
		checkPosition(r);
		checkStoneColor(color);
		return areVMillPositions(p, q, r) && IntStream.of(p, q, r).allMatch(pos -> content[pos] == color);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if the given position is inside a horizontal mill of the given color
	 */
	public boolean inHMill(int p, StoneColor color) {
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
	public boolean inVMill(int p, StoneColor color) {
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
	public boolean inMill(int p, StoneColor color) {
		return inHMill(p, color) || inVMill(p, color);
	}

	/**
	 * @param color
	 *          a stone color
	 * @return if all stones of the given color are inside some mill
	 */
	public boolean allStonesInMills(StoneColor color) {
		return positions(color).allMatch(p -> inMill(p, color));
	}

	/**
	 * @param color
	 *          a stone color
	 * @return a stream of all positions where a mill of the given color could be closed
	 */
	public IntStream positionsClosingMill(StoneColor color) {
		return positions().filter(p -> isMillClosingPosition(p, color));
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
	public boolean isHMillOpenedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isXMillOpenedAt(p, color, H_MILL);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if placing a stone of the given color at the given position opens a vertical mill
	 */
	public boolean isVMillOpenedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isXMillOpenedAt(p, color, V_MILL);
	}

	private boolean isXMillOpenedAt(int p, StoneColor color, int[][] mill) {
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
		return isHMillOpenedAt(p, color) || isVMillOpenedAt(p, color);
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
		return isHMillOpenedAt(p, color) && isVMillOpenedAt(p, color);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if a mill of the given color is closed by placing a stone of that color at the given position
	 */
	public boolean isMillClosingPosition(int p, StoneColor color) {
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
	public boolean isMillClosedByMove(int from, int to, StoneColor color) {
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
		return nextToNeighbors(p).filter(q -> getStoneAt(q) == color).anyMatch(q -> areTwoMillsPossibleLater(p, q, color));
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