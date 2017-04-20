package de.amr.games.pacman.entities.ghost.behaviors;

import static de.amr.games.pacman.PacManGame.Data;
import static de.amr.games.pacman.data.Board.Wall;

import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.fsm.State;

public class GhostLoopingAroundWalls extends State {

	private final Topology topology = new Top4();
	private final Ghost ghost;
	private final Tile target;
	private final boolean clockwise;
	private int pathIndex;
	private boolean targetReached;

	public GhostLoopingAroundWalls(Ghost ghost, int targetRow, int targetCol, int startDirection, boolean clockwise) {
		this.ghost = ghost;
		this.target = new Tile(targetRow, targetCol);
		this.clockwise = clockwise;
		this.targetReached = false;
		entry = state -> {
			pathIndex = 0;
			ghost.route.clear();
			targetReached = false;
			ghost.adjustOnTile();
		};
		update = state -> {
			if (targetReached) {
				cycle();
			} else if (ghost.isExactlyOverTile(target.getRow(), target.getCol())) {
				targetReached = true;
				ghost.moveDir = startDirection;
				ghost.nextMoveDir = startDirection;
				computePathAroundBlock(startDirection);
			} else {
				ghost.followRoute(target);
			}
		};
	}

	public Tile getTarget() {
		return new Tile(target);
	}

	public boolean isTargetReached() {
		return targetReached;
	}

	private void computePathAroundBlock(int dir_forward) {
		ghost.route.clear();
		Tile current = target;
		do {
			int dir_turn = clockwise ? topology.right(dir_forward) : topology.left(dir_forward);
			int dir_antiturn = topology.inv(dir_turn);
			Tile antiturn = new Tile(current).translate(topology.dx(dir_antiturn), topology.dy(dir_antiturn));
			Tile ahead = new Tile(current).translate(topology.dx(dir_forward), topology.dy(dir_forward));
			Tile around_corner = new Tile(ahead).translate(topology.dx(dir_turn), topology.dy(dir_turn));
			if (!Data.board.has(Wall, ahead)) {
				// can move ahead
				if (Data.board.has(Wall, around_corner)) {
					// no corner ahead, move forward
					ghost.route.add(dir_forward);
					current = ahead;
					if (current.equals(target)) {
						break;
					}
				} else {
					// corner ahead, move around corner
					ghost.route.add(dir_forward);
					current = ahead;
					if (current.equals(target)) {
						break;
					}
					ghost.route.add(dir_turn);
					dir_forward = dir_turn;
					current = around_corner;
					if (current.equals(target)) {
						break;
					}
				}
			} else if (!Data.board.has(Wall, antiturn)) {
				// move against turn direction
				ghost.route.add(dir_antiturn);
				dir_forward = dir_antiturn;
				current = antiturn;
				if (current.equals(target)) {
					break;
				}
			} else {
				throw new IllegalStateException("Stuck while computing path around the block");
			}
		} while (true);
	}

	private void cycle() {
		ghost.move();
		if (!ghost.isExactlyOverTile()) {
			return;
		}
		int dir = ghost.route.get(pathIndex < ghost.route.size() - 1 ? pathIndex + 1 : 0);
		if (ghost.canMoveTowards(dir)) {
			ghost.changeMoveDir(dir);
			pathIndex = (pathIndex + 1) == ghost.route.size() ? 0 : pathIndex + 1;
		}
	}
}