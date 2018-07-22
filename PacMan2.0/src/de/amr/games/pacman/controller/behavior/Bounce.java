package de.amr.games.pacman.controller.behavior;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

public class Bounce implements MoveBehavior {

	@Override
	public MoveData apply(MazeMover<?> bouncer) {
		MoveData result = new MoveData();
		result.dir = isReflected(bouncer) ? Maze.TOPOLOGY.inv(bouncer.getMoveDirection()) : bouncer.getMoveDirection();
		return result;
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