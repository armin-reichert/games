package de.amr.games.pacman.controller.behavior.impl;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.controller.behavior.Route;
import de.amr.games.pacman.model.Tile;

class RouteData implements Route {

	int dir;
	List<Tile> path = new ArrayList<>();

	@Override
	public int getNextMoveDirection() {
		return dir;
	}

	@Override
	public List<Tile> getPath() {
		return path;
	}
}