package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.board.Board;

public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp());
	}

	private Board board;
	private PlayScene scene;

	public PacManApp() {
		board = new Board(Assets.text("board.txt"));
		settings.title = "PacMan 2.0";
		settings.width = board.getNumCols() * Board.TILE_SIZE;
		settings.height = board.getNumRows() * Board.TILE_SIZE;
		settings.scale = 1.0f;
	}

	@Override
	public void init() {
		scene = new PlayScene(this);
		select(scene);
	}

	public Board getBoard() {
		return board;
	}
}