package de.amr.games.pacman.routing.impl;

import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.routing.Route;

class RouteData implements Route {

	int dir;
	List<Tile> path = Collections.emptyList();

	@Override
	public int getDirection() {
		return dir;
	}

	@Override
	public List<Tile> getPath() {
		return path;
	}
}