package de.amr.games.pacman.core.entities.ghost.behaviors;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.BoardMover;
import de.amr.games.pacman.core.statemachine.State;

/**
 * A state that lets the specified entity walk to the start tile (e.g. a corner tile) and then run
 * around the walls in that corner.
 * 
 * @author Armin Reichert
 */
public class LoopAroundWalls extends State {

	private final Tile loopStart;
	private boolean loopStarted;

	/**
	 * @param entity
	 *          the entity to be controlled
	 * @param loopStartRow
	 *          the start row of the loop
	 * @param loopStartCol
	 *          the start column of the loop
	 * @param loopStartDir
	 *          the start direction of the loop
	 * @param clockwise
	 *          if the ghost should walk clockwise or counter-clockwise
	 */
	public LoopAroundWalls(BoardMover entity, int loopStartRow, int loopStartCol, int loopStartDir, boolean clockwise) {

		this.loopStart = new Tile(loopStartRow, loopStartCol);

		// entry action
		entry = state -> {
			loopStarted = false;
			entity.getRoute().clear();
			entity.adjust();
		};

		// update action
		update = state -> {
			if (entity.isExactlyOverTile(loopStart)) {
				computePathAroundWalls(entity, loopStartDir, clockwise);
				entity.setMoveDir(loopStartDir);
				loopStarted = true;
			}
			if (loopStarted) {
				entity.moveAlongRoute();
			} else {
				entity.follow(loopStart);
			}
		};

		// exit action
		exit = state -> {
			entity.getRoute().clear();
		};
	}

	/**
	 * Computes the route around the walls when the ghost starts at the start tile and moves clockwise
	 * or counter-clockwise
	 * 
	 * @param entity
	 *          the entity to be controlled
	 * @param dir_forward
	 *          the initial forward direction
	 * @param clockwise
	 *          if the ghost should walk clockwise or counter-clockwise
	 */
	private void computePathAroundWalls(BoardMover entity, int dir_forward, boolean clockwise) {
		final Board board = entity.getBoard();
		Tile current = loopStart;
		entity.getRoute().clear();
		do {
			int dir_turn = clockwise ? Top4.INSTANCE.right(dir_forward) : Top4.INSTANCE.left(dir_forward);
			int dir_turn_inv = Top4.INSTANCE.inv(dir_turn);
			Tile current_antiturn = current.neighbor(dir_turn_inv);
			Tile current_ahead = current.neighbor(dir_forward);
			Tile current_around_corner = current_ahead.neighbor(dir_turn);
			if (!board.contains(current_ahead, TileContent.Wall)) {
				// can move ahead
				if (board.contains(current_around_corner, TileContent.Wall)) {
					// no corner in turn direction ahead, move forward
					entity.getRoute().add(dir_forward);
					current = current_ahead;
					if (current.equals(loopStart)) {
						break;
					}
				} else {
					// corner is ahead, move around corner
					entity.getRoute().add(dir_forward);
					current = current_ahead;
					if (current.equals(loopStart)) {
						break;
					}
					dir_forward = dir_turn;
					entity.getRoute().add(dir_forward);
					current = current_around_corner;
					if (current.equals(loopStart)) {
						break;
					}
				}
			} else if (!board.contains(current_antiturn, TileContent.Wall)) {
				// turn against loop direction
				dir_forward = Top4.INSTANCE.inv(dir_turn);
				entity.getRoute().add(dir_forward);
				current = current_antiturn;
				if (current.equals(loopStart)) {
					break;
				}
			} else {
				throw new IllegalStateException("Got stuck while computing path around walls");
			}
		} while (true);
	}
}