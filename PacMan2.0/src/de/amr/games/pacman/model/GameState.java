package de.amr.games.pacman.model;

import de.amr.easy.game.assets.Assets;

public class GameState {

	public Maze maze;
	public int level;
	public int lives;

	public GameState() {
		maze = new Maze(Assets.text("maze.txt"));
		level = 1;
		lives = 3;
	}
}
