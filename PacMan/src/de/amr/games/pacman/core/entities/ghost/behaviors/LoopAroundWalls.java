package de.amr.games.pacman.core.entities.ghost.behaviors;

import static de.amr.easy.grid.impl.Top4.Top4;

import java.util.ArrayList;
import java.util.List;

import de.amr.easy.statemachine.State;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.BoardMover;

/**
 * A state that lets the specified entity walk to the start tile (e.g. a corner tile) and then run
 * around the walls in that corner.
 * 
 * @author Armin Reichert
 */
public class LoopAroundWalls extends State {

	private final BoardMover mover;
	private final Tile loopStart;
	private final int loopStartDir;
	private boolean looping;
	private List<Integer> loopRoute;
	private List<Tile> loopTiles;

	/**
	 * @param mover
	 *          the entity to be moved
	 * @param loopStart
	 *          the start tile of the loop
	 * @param loopStartDir
	 *          the start direction of the loop
	 * @param clockwise
	 *          if the ghost should walk clockwise or counter-clockwise
	 */
	public LoopAroundWalls(BoardMover mover, Tile loopStart, int loopStartDir, boolean clockwise) {

		this.mover = mover;
		this.loopStart = loopStart;
		this.loopStartDir = loopStartDir;

		// entry action
		entry = state -> {
			looping = false;
			mover.getRoute().clear();
			mover.adjust();
		};

		// update action
		update = state -> {
			if (mover.isExactlyOver(loopStart)) {
				computePathAroundWalls(mover, loopStartDir, clockwise);
				mover.setRoute(loopRoute);
				mover.setMoveDir(loopStartDir);
				looping = true;
			}
			if (looping) {
				mover.moveAlongRoute();
			} else {
				mover.follow(loopStart);
			}
		};

		// exit action
		exit = state -> {
			mover.getRoute().clear();
		};
	}

	public BoardMover getMover() {
		return mover;
	}

	public Tile getLoopStart() {
		return loopStart;
	}

	public int getLoopStartDir() {
		return loopStartDir;
	}

	public List<Integer> getLoopRoute() {
		return loopRoute;
	}

	public List<Tile> getLoopTiles() {
		return loopTiles;
	}

	public boolean isLooping() {
		return looping;
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
		loopRoute = new ArrayList<>();
		do {
			int dir_turn = clockwise ? Top4.right(dir_forward) : Top4.left(dir_forward);
			int dir_turn_inv = Top4.inv(dir_turn);
			Tile current_antiturn = current.neighbor(dir_turn_inv);
			Tile current_ahead = current.neighbor(dir_forward);
			Tile current_around_corner = current_ahead.neighbor(dir_turn);
			if (!board.contains(current_ahead, TileContent.Wall)) {
				// can move ahead
				if (board.contains(current_around_corner, TileContent.Wall)) {
					// no corner in turn direction ahead, move forward
					loopRoute.add(dir_forward);
					current = current_ahead;
					if (current.equals(loopStart)) {
						break;
					}
				} else {
					// corner is ahead, move around corner
					loopRoute.add(dir_forward);
					current = current_ahead;
					if (current.equals(loopStart)) {
						break;
					}
					dir_forward = dir_turn;
					loopRoute.add(dir_forward);
					current = current_around_corner;
					if (current.equals(loopStart)) {
						break;
					}
				}
			} else if (!board.contains(current_antiturn, TileContent.Wall)) {
				// turn against loop direction
				dir_forward = Top4.inv(dir_turn);
				loopRoute.add(dir_forward);
				current = current_antiturn;
				if (current.equals(loopStart)) {
					break;
				}
			} else {
				throw new IllegalStateException("Got stuck while computing path around walls");
			}
		} while (true);

		// create route as tile list
		loopTiles = new ArrayList<>();
		current = loopStart;
		loopTiles.add(current);
		for (int dir : loopRoute) {
			Tile next = new Tile(current).translate(Top4.dy(dir), Top4.dx(dir));
			loopTiles.add(next);
			current = next;
		}
	}
}