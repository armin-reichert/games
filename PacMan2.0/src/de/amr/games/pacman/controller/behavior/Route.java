package de.amr.games.pacman.controller.behavior;

import java.util.List;

import de.amr.games.pacman.model.Tile;

public interface Route {

	int getNextMoveDirection();

	List<Tile> getPath();
}