package de.amr.games.pacman.model;

public class Game {

	public int level;
	public int lives;
	public int score;
	public int dotsEatenInLevel;

	public Game() {
		level = 1;
		lives = 3;
		score = 0;
		dotsEatenInLevel = 0;
	}
}