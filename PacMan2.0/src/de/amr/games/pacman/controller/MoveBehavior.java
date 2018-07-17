package de.amr.games.pacman.controller;

import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.model.Tile;

public interface MoveBehavior {

	int getNextMoveDirection();

	default List<Tile> getPathToTarget() {
		return Collections.emptyList();
	}
}
