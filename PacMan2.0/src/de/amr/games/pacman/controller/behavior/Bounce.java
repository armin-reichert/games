package de.amr.games.pacman.controller.behavior;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

public class Bounce implements MoveBehavior {

	@Override
	public int getNextMoveDirection(MazeMover<?> bouncer) {
		if (isReflected(bouncer)) {
			return Maze.TOPOLOGY.inv(bouncer.getMoveDirection());
		} else {
			return bouncer.getMoveDirection();
		}
	}

	private boolean isReflected(MazeMover<?> bouncer) {
		Tile touchedTile = bouncer.computeTouchedTile(bouncer.getTile(), bouncer.getMoveDirection());
		if (touchedTile.equals(bouncer.getTile())) {
			return false;
		}
		char c = bouncer.getMaze().getContent(touchedTile);
		return c == Tile.WALL || c == Tile.DOOR;
	}
}
