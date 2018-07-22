package de.amr.games.pacman.controller.behavior;

import java.util.List;

import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

public class GoHome implements MoveBehavior {

	private List<Tile> targetPath;

	@Override
	public int getNextMoveDirection(MazeMover<?> mover) {
		targetPath = mover.getMaze().findPath(mover.getTile(), mover.getHome());
		return mover.getMaze().dirAlongPath(targetPath).orElse(mover.getNextMoveDirection());
	}

	@Override
	public List<Tile> getTargetPath() {
		return targetPath;
	}
}