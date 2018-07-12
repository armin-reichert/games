package de.amr.games.pacman;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.scene.ActiveScene;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.board.Maze;
import de.amr.games.pacman.board.Tile;
import de.amr.games.pacman.entities.PacMan;

public class PlayScene extends ActiveScene<PacManApp> {

	private static boolean DEBUG = false;

	private Maze maze;
	private PacMan pacMan;

	public PlayScene(PacManApp app) {
		super(app);
	}

	@Override
	public void init() {
		maze = new Maze(app.getBoard(), getWidth(), getHeight() - 5 * Board.TILE_SIZE);
		pacMan = new PacMan(app.getBoard());
		maze.init();
		pacMan.init();
	}

	@Override
	public void update() {
		maze.update();
		if (maze.getBoard().isEmpty()) {
			maze.getBoard().resetContent();
		}
		pacMan.update();
	}

	@Override
	public void draw(Graphics2D g) {
		// first three rows reserved for HUD
		g.translate(0, 3 * Board.TILE_SIZE);
		maze.draw(g);
		pacMan.draw(g);
		g.translate(0, -3 * Board.TILE_SIZE);
		if (DEBUG) {
			drawGridLines(g);
		}
	}

	private void drawGridLines(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		for (int row = 0; row < maze.getBoard().getNumRows(); ++row) {
			g.drawLine(0, row * Board.TILE_SIZE, getWidth(), row * Board.TILE_SIZE);
		}
		for (int col = 0; col < maze.getBoard().getNumCols(); ++col) {
			g.drawLine(col * Board.TILE_SIZE, 0, col * Board.TILE_SIZE, getHeight());
		}
	}

}