package de.amr.games.pacman.model;

import de.amr.easy.game.assets.Assets;

public class Game {

	public Maze maze;
	public int level;
	public int lives;
	public int score;

	public void init() {
		maze = new Maze(Assets.text("maze.txt"));
		level = 1;
		lives = 3;
		score = 0;
	}
}
