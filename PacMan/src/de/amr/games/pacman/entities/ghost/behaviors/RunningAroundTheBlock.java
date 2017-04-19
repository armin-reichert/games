package de.amr.games.pacman.entities.ghost.behaviors;

import static de.amr.easy.game.Application.Log;
import static de.amr.games.pacman.PacManGame.Data;
import static de.amr.games.pacman.data.Board.Wall;

import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.fsm.State;

public class RunningAroundTheBlock extends State {

	private final Topology tpl = new Top4();
	private final Ghost ghost;
	private final Tile target;
	private final int cycleStartDir;
	private final boolean cycleClockwise;

	private int pathIndex;
	private boolean targetReached;

	public RunningAroundTheBlock(Ghost ghost, int targetRow, int targetCol, int cycleStartDir, boolean cycleClockwise) {
		this.ghost = ghost;
		this.target = new Tile(targetRow, targetCol);
		this.cycleStartDir = cycleStartDir;
		this.cycleClockwise = cycleClockwise;
		this.targetReached = false;
		entry = state -> reset();
		update = state -> reachTargetThenCycle();
	}

	public Tile getTarget() {
		return new Tile(target);
	}

	public boolean isTargetReached() {
		return targetReached;
	}

	private void reset() {
		pathIndex = 0;
		ghost.route.clear();
		targetReached = false;
		ghost.adjustOnTile();
	}

	private void reachTargetThenCycle() {
		if (targetReached) {
			cycle();
		} else if (ghost.isExactlyOverTile(target.getRow(), target.getCol())) {
			targetReached = true;
			ghost.moveDir = cycleStartDir;
			ghost.nextMoveDir = cycleStartDir;
			computePathAroundBlock(target, cycleStartDir, cycleClockwise);
		} else {
			ghost.followRoute(target);
		}
	}

	private void computePathAroundBlock(Tile start, int dir_ahead, boolean clockwise) {
		ghost.route.clear();
		Tile current = start;
		do {
			int dir_turn = clockwise ? tpl.right(dir_ahead) : tpl.left(dir_ahead);
			int dir_antiturn = tpl.inv(dir_turn);
			Tile antiturn = new Tile(current).translate(tpl.dx(dir_antiturn), tpl.dy(dir_antiturn));
			Tile ahead = new Tile(current).translate(tpl.dx(dir_ahead), tpl.dy(dir_ahead));
			Tile ahead_turn = new Tile(ahead).translate(tpl.dx(dir_turn), tpl.dy(dir_turn));
			if (!Data.board.has(Wall, ahead)) {
				// can move ahead or around a corner
				if (Data.board.has(Wall, ahead_turn)) {
					// move ahead
					addToRoute(dir_ahead);
					current = ahead;
					if (current.equals(start)) {
						break;
					}
				} else {
					// move around corner
					addToRoute(dir_ahead);
					current = ahead;
					if (current.equals(start)) {
						break;
					}
					addToRoute(dir_turn);
					dir_ahead = dir_turn;
					current = ahead_turn;
					if (current.equals(start)) {
						break;
					}
				}
			} else if (!Data.board.has(Wall, antiturn)) {
				// move against turn direction
				addToRoute(dir_antiturn);
				dir_ahead = dir_antiturn;
				current = antiturn;
				if (current.equals(start)) {
					break;
				}
			} else {
				throw new IllegalStateException("Stuck while computing path around the block");
			}
		} while (true);
	}

	private void addToRoute(int dir) {
		ghost.route.add(dir);
		Log.info(tpl.getName(dir));
	}

	private void cycle() {
		ghost.move();
		if (!ghost.isExactlyOverTile()) {
			return;
		}
		int dir = getNextPathDir();
		if (ghost.canMoveTowards(dir)) {
			ghost.changeMoveDir(dir);
			pathIndex = (pathIndex + 1) == ghost.route.size() ? 0 : pathIndex + 1;
		}
	}

	private int getNextPathDir() {
		return ghost.route.get(pathIndex < ghost.route.size() - 1 ? pathIndex + 1 : 0);
	}
}