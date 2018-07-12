package de.amr.games.pacman;

import static de.amr.games.pacman.board.Tile.BONUS_PEACH;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.OptionalInt;

import de.amr.easy.game.scene.ActiveScene;
import de.amr.easy.util.StreamUtils;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.board.Maze;
import de.amr.games.pacman.board.SpriteSheet;
import de.amr.games.pacman.board.Tile;
import de.amr.games.pacman.entities.BoardMover;
import de.amr.games.pacman.entities.Ghost;
import de.amr.games.pacman.entities.PacMan;

public class PlayScene extends ActiveScene<PacManApp> {

	private static boolean DEBUG = false;

	private Maze maze;
	private PacMan pacMan;
	private Ghost redGhost;
	private Ghost pinkGhost;
	private Ghost blueGhost;
	private Ghost orangeGhost;

	public PlayScene(PacManApp app) {
		super(app);
	}

	@Override
	public void init() {
		app.getBoard().setTile(13, 17, BONUS_PEACH);
		
		maze = new Maze(app.getBoard(), getWidth(), getHeight() - 5 * Board.TILE_SIZE);
		
		pacMan = new PacMan(app.getBoard());
		pacMan.setMazePosition(14, 23);
		
		redGhost = new Ghost(app.getBoard(), SpriteSheet.RED);
		pinkGhost = new Ghost(app.getBoard(), SpriteSheet.PINK);
		blueGhost = new Ghost(app.getBoard(), SpriteSheet.BLUE);
		orangeGhost = new Ghost(app.getBoard(), SpriteSheet.ORANGE);

		pacMan.enemies.addAll(Arrays.asList(redGhost, pinkGhost, blueGhost, orangeGhost));
		
		findFreeTile().ifPresent(
				tile -> redGhost.setMazePosition(app.getBoard().getGrid().col(tile), app.getBoard().getGrid().row(tile)));
		findFreeTile().ifPresent(
				tile -> pinkGhost.setMazePosition(app.getBoard().getGrid().col(tile), app.getBoard().getGrid().row(tile)));
		findFreeTile().ifPresent(
				tile -> blueGhost.setMazePosition(app.getBoard().getGrid().col(tile), app.getBoard().getGrid().row(tile)));
		findFreeTile().ifPresent(
				tile -> orangeGhost.setMazePosition(app.getBoard().getGrid().col(tile), app.getBoard().getGrid().row(tile)));

		maze.init();
		pacMan.init();
		redGhost.init();
		pinkGhost.init();
		blueGhost.init();
		orangeGhost.init();
	}

	private OptionalInt findFreeTile() {
		return StreamUtils
				.permute(app.getBoard().getGrid().vertices().filter(v -> app.getBoard().getGrid().get(v) == Tile.EMPTY))
				.findAny();
	}

	@Override
	public void update() {
		maze.update();
		if (maze.getBoard().isEmpty()) {
			maze.getBoard().resetContent();
		}
		pacMan.update();
		redGhost.update();
		pinkGhost.update();
		blueGhost.update();
		orangeGhost.update();
	}

	@Override
	public void draw(Graphics2D g) {
		// first three rows reserved for HUD
		g.translate(0, 3 * Board.TILE_SIZE);
		maze.draw(g);
		pacMan.draw(g);
		redGhost.draw(g);
		pinkGhost.draw(g);
		blueGhost.draw(g);
		orangeGhost.draw(g);
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