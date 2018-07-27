package de.amr.games.pacman.behavior.impl;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.model.Tile;

class RouteData implements Route {

	int dir;
	List<Tile> path = new ArrayList<>();

	@Override
	public int getDirection() {
		return dir;
	}

	@Override
	public List<Tile> getPath() {
		return path;
	}
}