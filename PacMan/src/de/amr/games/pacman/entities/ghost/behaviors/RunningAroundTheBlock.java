package de.amr.games.pacman.entities.ghost.behaviors;

import de.amr.easy.grid.api.Dir4;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.fsm.State;

public class RunningAroundTheBlock extends State {

	private final Ghost ghost;
	private final Tile target;
	private final Dir4 cycleStartDir;
	private final boolean cycleClockwise;

	private int pathIndex;
	private Tile next;
	private boolean targetReached;

	public RunningAroundTheBlock(Ghost ghost, int targetRow, int targetCol, Dir4 cycleStartDir,
			boolean cycleClockwise) {
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
			ghost.moveDir = cycleStartDir;
			ghost.nextMoveDir = cycleStartDir;
			computePathAroundWall(target, cycleStartDir, cycleClockwise);
			targetReached = true;
		} else {
			ghost.followRoute(target);
		}
	}

	private void computePathAroundWall(Tile start, Dir4 dir, boolean clockwise) {
		ghost.route.clear();
		pathIndex = 0;
		next = new Tile(start);
		extendPath(dir);
		while (!next.equals(start)) {
			Dir4 turn = clockwise ? dir.right() : dir.left();
			if (ghost.canEnter(new Tile(next).translate(turn.dx, turn.dy))) {
				dir = extendPath(turn);
			} else if (ghost.canEnter(new Tile(next).translate(dir.dx, dir.dy))) {
				dir = extendPath(dir);
			} else if (ghost.canEnter(new Tile(next).translate(turn.inverse().dx, turn.inverse().dy))) {
				dir = extendPath(turn.inverse());
			}
		}
	}

	private Dir4 extendPath(Dir4 dir) {
		ghost.route.add(dir);
		next.translate(dir.dx, dir.dy);
		return dir;
	}

	private void cycle() {
		ghost.move();
		if (!ghost.isExactlyOverTile()) {
			return;
		}
		Dir4 dir = getNextPathDir();
		if (ghost.canMoveTowards(dir)) {
			ghost.changeMoveDir(dir);
			pathIndex = (pathIndex + 1) == ghost.route.size() ? 0 : pathIndex + 1;
		}
	}

	private Dir4 getNextPathDir() {
		return ghost.route.get(pathIndex < ghost.route.size() - 1 ? pathIndex + 1 : 0);
	}
}