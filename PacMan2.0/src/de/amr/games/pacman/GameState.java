package de.amr.games.pacman;

import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.board.MazeContent;

public class GameState {
	
	public MazeContent mazeContent;
	public int level;
	public int lives;
	
	public GameState() {
		mazeContent = new MazeContent(Assets.text("maze.txt"));
		level = 1;
		lives = 3;
	}
}
