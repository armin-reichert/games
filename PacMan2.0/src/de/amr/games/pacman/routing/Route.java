package de.amr.games.pacman.routing;

import java.util.List;

import de.amr.games.pacman.model.Tile;

public interface Route {

	int getDirection();

	List<Tile> getPath();
}