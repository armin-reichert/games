package de.amr.games.muehle.board;

import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents the board, provides information about the board content and mill-related functionality.
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

	private void set(int p, StoneColor color) {
		content[p] = color;
	}

	private StoneColor get(int p) {
		return content[p];
	}

	private boolean has(int p, StoneColor color) {
		return content[p] == color;
	}

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
		Arrays.fill(content, null);
	}

	/**
	 * @param color
	 *          a stone color
	 * @return a stream of the positions with a stone of the given color
	 */
	public IntStream positions(StoneColor color) {
		checkStoneColor(color);
		return positions().filter(p -> has(p, color));
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
	 *          a position
	 * @param color
	 *          a stone color
	 */
	public void putStoneAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		if (!has(p, null)) {
			throw new IllegalStateException("Position where stone is placed must be empty");
		}
		set(p, color);
	}

	/**
	 * Removes a stone from the given position. Empty positions are allowed.
	 * 
	 * @param p
	 *          a valid position
	 */
	public void removeStoneAt(int p) {
		checkPosition(p);
		set(p, null);
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
		if (has(from, null)) {
			throw new IllegalStateException("Position from where stone is moved must not be empty");
		}
		if (!has(to, null)) {
			throw new IllegalStateException("Position where stone is moved to must be empty");
		}
		set(to, get(from));
		set(from, null);
	}

	/**
	 * @param p
	 *          a position
	 * @return the (optional) content at this position
	 */
	public Optional<StoneColor> getStoneAt(int p) {
		checkPosition(p);
		return Optional.ofNullable(get(p));
	}

	/**
	 * @param p
	 *          a valid position
	 * @return if the position is empty
	 */
	public boolean isEmptyPosition(int p) {
		checkPosition(p);
		return has(p, null);
	}

	/**
	 * @return stream of all empty positions on the board
	 */
	public IntStream emptyPositions() {
		return positions().filter(p -> has(p, null));
	}

	/**
	 * @param p
	 *          a valid position
	 * @return if there is a stone at the position
	 */
	public boolean hasStoneAt(int p) {
		checkPosition(p);
		return !has(p, null);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if there is a stone of the given color at this position
	 */
	public boolean hasStoneAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return has(p, color);
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
		return inRow(p, q, r) && IntStream.of(p, q, r).allMatch(pos -> has(pos, color));
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
		return inCol(p, q, r) && IntStream.of(p, q, r).allMatch(pos -> has(pos, color));
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
		return IntStream.of(p, ROW[p][0], ROW[p][1]).allMatch(q -> has(q, color));
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
		return IntStream.of(p, COL[p][0], COL[p][1]).allMatch(q -> has(q, color));
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
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if the given position is part of an open mill of the given color
	 */
	public boolean partOfOpenMill(int p, StoneColor color) {
		return partOfOpenHMill(p, color) || partOfOpenVMill(p, color);
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if the given position is part of an open horizontal mill of the given color
	 */
	public boolean partOfOpenHMill(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return has(p, color) && IntStream.of(ROW[p]).filter(q -> has(q, color)).count() == 1
				&& IntStream.of(ROW[p]).filter(this::isEmptyPosition).count() == 1;
	}

	/**
	 * @param p
	 *          a valid position
	 * @param color
	 *          a stone color
	 * @return if the given position is part of an open vertical mill of the given color
	 */
	public boolean partOfOpenVMill(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return has(p, color) && IntStream.of(COL[p]).filter(q -> has(q, color)).count() == 1
				&& IntStream.of(COL[p]).filter(this::isEmptyPosition).count() == 1;
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
		return isXMillOpenedAt(p, color, ROW[p]);
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
		return isXMillOpenedAt(p, color, COL[p]);
	}

	private boolean isXMillOpenedAt(int p, StoneColor color, int[] mill) {
		int q = mill[0], r = mill[1];
		return (has(p, null) && has(q, color) && has(r, null)) || (has(p, null) && has(q, null) && has(r, color));
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
		return isHMillClosingPosition(p, color) || isVMillClosingPosition(p, color);
	}

	private boolean isHMillClosingPosition(int p, StoneColor color) {
		return has(p, null) && has(ROW[p][0], color) && has(ROW[p][1], color);
	}

	private boolean isVMillClosingPosition(int p, StoneColor color) {
		return has(p, null) && has(COL[p][0], color) && has(COL[p][1], color);
	}

	/**
	 * @param fromMil
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
		if (has(from, color) && has(to, null)) {
			Optional<Direction> optDir = getDirection(from, to);
			if (optDir.isPresent()) {
				Direction dir = optDir.get();
				if (dir == NORTH || dir == SOUTH) {
					return has(ROW[to][0], color) && has(ROW[to][1], color);
				} else {
					return has(COL[to][0], color) && has(COL[to][1], color);
				}
			}
		}
		return false;
	}

	public boolean isMillClosedByJump(int from, int to, StoneColor color) {
		checkPosition(from);
		checkPosition(to);
		checkStoneColor(color);
		return has(from, color) && isMillClosingPosition(to, color);
	}

	/**
	 * @param p
	 *          valid position
	 * @param color
	 *          stone color
	 * @return if a mill of the given color can be closed when moving from p
	 */
	public boolean canCloseMillMovingFrom(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return has(p, color) && (getVMillClosingNeighbors(p, color).findFirst().isPresent()
				|| getHMillClosingNeighbors(p, color).findFirst().isPresent());
	}

	private IntStream getVMillClosingNeighbors(int p, StoneColor color) {
		return emptyNeighbors(p).filter(n -> isVMillClosingPosition(n, color)).filter(n -> {
			Direction dir = getDirection(n, p).get();
			return dir == Direction.WEST || dir == Direction.EAST;
		});
	}

	private IntStream getHMillClosingNeighbors(int p, StoneColor color) {
		return emptyNeighbors(p).filter(n -> isHMillClosingPosition(n, color)).filter(n -> {
			Direction dir = getDirection(n, p).get();
			return dir == Direction.NORTH || dir == Direction.SOUTH;
		});
	}

	public boolean canCloseMillJumpingFrom(int from, StoneColor color) {
		checkPosition(from);
		checkStoneColor(color);
		return has(from, color) && positionsClosingMill(color).findAny().isPresent();
	}

	/**
	 * @param color
	 *          stone color
	 * @return positions where by placing a stone two mills of the same color could be opened later
	 */
	public IntStream positionsOpeningTwoMillsLater(StoneColor color) {
		checkStoneColor(color);
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
		checkPosition(p);
		checkStoneColor(color);
		return nextToNeighbors(p).filter(q -> get(q) == color).anyMatch(q -> areTwoMillsPossibleLater(p, q, color));
	}

	private boolean areTwoMillsPossibleLater(int p, int q, StoneColor color) {
		checkPosition(p);
		checkPosition(q);
		checkStoneColor(color);
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