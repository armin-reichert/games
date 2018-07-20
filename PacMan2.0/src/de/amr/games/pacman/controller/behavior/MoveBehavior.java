package de.amr.games.pacman.controller.behavior;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.model.Tile;

public interface MoveBehavior {

	int getNextMoveDirection();

	default Optional<Tile> getTargetTile() {
		return Optional.empty();
	}

	default List<Tile> getTargetPath() {
		return Collections.emptyList();
	}
}
