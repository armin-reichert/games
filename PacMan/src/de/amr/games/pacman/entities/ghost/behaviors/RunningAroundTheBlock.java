package de.amr.games.pacman.entities.ghost.behaviors;

import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.fsm.State;

public class RunningAroundTheBlock extends State {

	private final Topology top = new Top4();
	private final Ghost ghost;
	private final Tile target;
	private final int cycleStartDir;
	private final boolean cycleClockwise;

	private int pathIndex;
	private Tile next;
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
			ghost.moveDir = cycleStartDir;
			ghost.nextMoveDir = cycleStartDir;
			computePathAroundWall(target, cycleStartDir, cycleClockwise);
			targetReached = true;
		} else {
			ghost.followRoute(target);
		}
	}

	private void computePathAroundWall(Tile start, int dir, boolean clockwise) {
		ghost.route.clear();
		pathIndex = 0;
		next = new Tile(start);
		extendPath(dir);
		while (!next.equals(start)) {
			int turn = clockwise ? top.right(dir) : top.left(dir);
			if (ghost.canEnter(new Tile(next).translate(top.dx(turn), top.dy(turn)))) {
				dir = extendPath(turn);
			} else if (ghost.canEnter(new Tile(next).translate(top.dx(turn), top.dy(turn)))) {
				dir = extendPath(dir);
			} else if (ghost.canEnter(new Tile(next).translate(top.dx(top.inv(turn)), top.dy(top.inv(turn))))) {
				dir = extendPath(top.inv(turn));
			}
		}
	}

	private int extendPath(int dir) {
		ghost.route.add(dir);
		next.translate(top.dx(dir), top.dy(dir));
		return dir;
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