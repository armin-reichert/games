package de.amr.games.pacman.controller.behavior;

import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

public interface MoveBehavior {

	int getNextMoveDirection(MazeMover<?> mover);

	default List<Tile> getTargetPath() {
		return Collections.emptyList();
	}
}
