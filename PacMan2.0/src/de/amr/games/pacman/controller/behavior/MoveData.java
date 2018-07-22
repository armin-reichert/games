package de.amr.games.pacman.controller.behavior;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.model.Tile;

public class MoveData {

	int dir;
	List<Tile> path = new ArrayList<>();

	public int getNextMoveDirection() {
		return dir;
	}

	public List<Tile> getPath() {
		return path;
	}
}