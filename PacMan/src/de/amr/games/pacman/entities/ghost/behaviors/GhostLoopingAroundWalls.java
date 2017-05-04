package de.amr.games.pacman.entities.ghost.behaviors;

import static de.amr.games.pacman.PacManGame.Game;

import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.data.TileContent;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.fsm.State;

/**
 * A ghost behavior that lets the specified ghost walk to the start tile (e.g. a corner tile) and
 * then lets it run around the walls in that corner.
 * 
 * @author Armin Reichert
 *
 */
public class GhostLoopingAroundWalls extends State {

	private final Tile loopStart;
	private boolean loopStarted;
	private int routeIndex;

	/**
	 * @param ghost
	 *          the ghost to be controlled
	 * @param loopStartRow
	 *          the start row of the loop
	 * @param loopStartCol
	 *          the start column of the loop
	 * @param loopStartDir
	 *          the start direction of the loop
	 * @param clockwise
	 *          if the ghost should walk clockwise or counter-clockwise
	 */
	public GhostLoopingAroundWalls(Ghost ghost, int loopStartRow, int loopStartCol, int loopStartDir, boolean clockwise) {

		this.loopStart = new Tile(loopStartRow, loopStartCol);

		// entry action
		entry = state -> {
			loopStarted = false;
			routeIndex = 0;
			ghost.route.clear();
			ghost.adjustOnTile();
		};

		// update action
		update = state -> {
			if (loopStarted) {
				// move along computed loop route
				ghost.move();
				if (ghost.isExactlyOverTile()) {
					// check if direction should be changed
					int dir = ghost.route.get(routeIndex < ghost.route.size() ? routeIndex : 0);
					ghost.changeMoveDir(dir);
					routeIndex = (routeIndex + 1) == ghost.route.size() ? 0 : routeIndex + 1;
				}
			} else if (ghost.isExactlyOverTile(loopStartRow, loopStartCol)) {
				// loop start tile reached for the first time, start looping
				loopStarted = true;
				ghost.moveDir = ghost.nextMoveDir = loopStartDir;
				computePathAroundWalls(ghost, loopStartDir, clockwise);
			} else {
				ghost.followRoute(loopStart);
			}
		};
		
		// exit action
		exit = state -> {
			ghost.route.clear();
		};
	}

	/**
	 * Computes the route around the walls when the ghost starts at the start tile and moves clockwise
	 * or counter-clockwise
	 * 
	 * @param ghost
	 *          the ghost to be controlled
	 * @param dir_forward
	 *          the initial forward direction
	 * @param clockwise
	 *          if the ghost should walk clockwise or counter-clockwise
	 */
	private void computePathAroundWalls(Ghost ghost, int dir_forward, boolean clockwise) {
		ghost.route.clear();
		Tile current = loopStart;
		do {
			int dir_turn = clockwise ? Game.board.topology.right(dir_forward) : Game.board.topology.left(dir_forward);
			int dir_turn_inv = Game.board.topology.inv(dir_turn);
			Tile current_antiturn = new Tile(current).translate(Game.board.topology.dx(dir_turn_inv),
					Game.board.topology.dy(dir_turn_inv));
			Tile current_ahead = new Tile(current).translate(Game.board.topology.dx(dir_forward),
					Game.board.topology.dy(dir_forward));
			Tile current_around_corner = new Tile(current_ahead).translate(Game.board.topology.dx(dir_turn),
					Game.board.topology.dy(dir_turn));
			if (!Game.board.contains(current_ahead, TileContent.Wall)) {
				// can move ahead
				if (Game.board.contains(current_around_corner, TileContent.Wall)) {
					// no corner in turn direction ahead, move forward
					ghost.route.add(dir_forward);
					current = current_ahead;
					if (current.equals(loopStart)) {
						break;
					}
				} else {
					// corner is ahead, move around corner
					ghost.route.add(dir_forward);
					current = current_ahead;
					if (current.equals(loopStart)) {
						break;
					}
					dir_forward = dir_turn;
					ghost.route.add(dir_forward);
					current = current_around_corner;
					if (current.equals(loopStart)) {
						break;
					}
				}
			} else if (!Game.board.contains(current_antiturn, TileContent.Wall)) {
				// turn against loop direction
				dir_forward = Game.board.topology.inv(dir_turn);
				ghost.route.add(dir_forward);
				current = current_antiturn;
				if (current.equals(loopStart)) {
					break;
				}
			} else {
				throw new IllegalStateException("Got stuck while computing path around walls");
			}
		} while (true);
	}

	/**
	 * 
	 * @return the start tile of the loop
	 */
	public Tile getLoopStart() {
		return new Tile(loopStart);
	}

	/**
	 * @return if the looping has started
	 */
	public boolean hasLoopStarted() {
		return loopStarted;
	}
}