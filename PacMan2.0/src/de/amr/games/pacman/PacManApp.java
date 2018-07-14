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
		board = new Board(Assets.text("maze.txt"));
		settings.width = board.numCols() * Board.TS;
		settings.height = board.numRows() * Board.TS;
		settings.scale = 1.25f;
		settings.title = String.format("PacMan 2.0 (%d x %d * %.2f)", settings.width, settings.height, settings.scale);
	}

	@Override
	public void init() {
		select(new PlayScene(this));
	}
}