package de.amr.games.pacman.entities.ghost.behaviors;

import static de.amr.games.pacman.PacManGame.Data;
import static de.amr.games.pacman.data.Board.Wall;

import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.fsm.State;

public class GhostLoopingAroundWalls extends State {

	private static final Topology topology = new Top4();

	private final Tile loopStart;
	private boolean loopStarted;
	private int routeIndex;

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
	}

	private void computePathAroundWalls(Ghost ghost, int dir_forward, boolean clockwise) {
		ghost.route.clear();
		Tile current = loopStart;
		do {
			int dir_turn = clockwise ? topology.right(dir_forward) : topology.left(dir_forward);
			int dir_turn_inv = topology.inv(dir_turn);
			Tile current_antiturn = new Tile(current).translate(topology.dx(dir_turn_inv), topology.dy(dir_turn_inv));
			Tile current_ahead = new Tile(current).translate(topology.dx(dir_forward), topology.dy(dir_forward));
			Tile current_around_corner = new Tile(current_ahead).translate(topology.dx(dir_turn), topology.dy(dir_turn));
			if (!Data.board.has(Wall, current_ahead)) {
				// can move ahead
				if (Data.board.has(Wall, current_around_corner)) {
					// no corner in turn direction ahead, move forward
					ghost.route.add(dir_forward);
					current = current_ahead;
					if (current.equals(loopStart)) {
						break;
					}
				} else {
					// corner ahead, move around corner
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
			} else if (!Data.board.has(Wall, current_antiturn)) {
				// turn against loop direction
				dir_forward = topology.inv(dir_turn);
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

	public Tile getLoopStart() {
		return new Tile(loopStart);
	}

	public boolean hasLoopStarted() {
		return loopStarted;
	}
}