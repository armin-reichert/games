package de.amr.games.pacman.entities.ghost.behaviors;

import static de.amr.games.pacman.PacManGame.Data;
import static de.amr.games.pacman.data.Board.Wall;

import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.fsm.State;

public class GhostLoopingAroundWalls extends State {

	private final Tile loopStart;
	private boolean loopStartReached;
	private int routeIndex;

	public GhostLoopingAroundWalls(Ghost ghost, int loopStartRow, int loopStartCol, int loopStartDir, boolean clockwise) {
		this.loopStart = new Tile(loopStartRow, loopStartCol);
		this.loopStartReached = false;

		// entry action
		entry = state -> {
			routeIndex = 0;
			loopStartReached = false;
			ghost.route.clear();
			ghost.adjustOnTile();
		};

		// update action
		update = state -> {
			if (loopStartReached) {
				// move along computed loop
				ghost.move();
				if (!ghost.isExactlyOverTile()) {
					return;
				}
				int dir = ghost.route.get(routeIndex < ghost.route.size() - 1 ? routeIndex + 1 : 0);
				ghost.changeMoveDir(dir);
				routeIndex = (routeIndex + 1) == ghost.route.size() ? 0 : routeIndex + 1;
			} else if (ghost.isExactlyOverTile(loopStartRow, loopStartCol)) {
				loopStartReached = true;
				ghost.moveDir = loopStartDir;
				ghost.nextMoveDir = loopStartDir;
				computePathAroundWalls(ghost, loopStartDir, clockwise, new Top4());
			} else {
				ghost.followRoute(loopStart);
			}
		};
	}

	private void computePathAroundWalls(Ghost ghost, int dir_forward, boolean clockwise, Topology topology) {
		ghost.route.clear();
		Tile current = loopStart;
		do {
			int dir_turn = clockwise ? topology.right(dir_forward) : topology.left(dir_forward);
			int dir_antiturn = topology.inv(dir_turn);
			Tile antiturn = new Tile(current).translate(topology.dx(dir_antiturn), topology.dy(dir_antiturn));
			Tile ahead = new Tile(current).translate(topology.dx(dir_forward), topology.dy(dir_forward));
			Tile around_corner = new Tile(ahead).translate(topology.dx(dir_turn), topology.dy(dir_turn));
			if (!Data.board.has(Wall, ahead)) {
				// can move ahead
				if (Data.board.has(Wall, around_corner)) {
					// no corner in turn direction ahead, move forward
					ghost.route.add(dir_forward);
					current = ahead;
					if (current.equals(loopStart)) {
						break;
					}
				} else {
					// corner ahead, move around corner
					ghost.route.add(dir_forward);
					current = ahead;
					if (current.equals(loopStart)) {
						break;
					}
					ghost.route.add(dir_turn);
					dir_forward = dir_turn;
					current = around_corner;
					if (current.equals(loopStart)) {
						break;
					}
				}
			} else if (!Data.board.has(Wall, antiturn)) {
				// move against turn direction
				ghost.route.add(dir_antiturn);
				dir_forward = dir_antiturn;
				current = antiturn;
				if (current.equals(loopStart)) {
					break;
				}
			} else {
				throw new IllegalStateException("Stuck while computing path around the block");
			}
		} while (true);
	}

	public Tile getLoopStart() {
		return new Tile(loopStart);
	}

	public boolean isLoopStartReached() {
		return loopStartReached;
	}

}