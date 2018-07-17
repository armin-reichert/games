package de.amr.games.pacman.model;

public class Game {

	public final Maze maze;
	public int level;
	public int lives;
	public int score;

	public Game(Maze maze) {
		this.maze = maze;
		level = 1;
		lives = 3;
		score = 0;
	}
}