package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.board.Board;

public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp());
	}

	public final Board board;

	public PacManApp() {
		settings.title = "PacMan 2.0";
		board = new Board(Assets.text("maze.txt"));
		settings.width = board.numCols() * Board.TILE_SIZE;
		settings.height = board.numRows() * Board.TILE_SIZE;
		settings.scale = 1f;
	}

	@Override
	public void init() {
		select(new PlayScene(this));
	}
}