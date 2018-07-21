package de.amr.games.pacman.controller.behavior;

import java.util.List;

import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

public class GoHome implements MoveBehavior {

	private final MazeMover<?> mover;
	private List<Tile> targetPath;

	public GoHome(MazeMover<?> mover) {
		this.mover = mover;
	}

	@Override
	public int getNextMoveDirection() {
		targetPath = mover.getMaze().findPath(mover.getTile(), mover.getHome());
		return mover.getMaze().dirAlongPath(targetPath).orElse(mover.getNextMoveDirection());
	}

	@Override
	public List<Tile> getTargetPath() {
		return targetPath;
	}
}