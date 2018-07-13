package de.amr.games.pacman;

import static de.amr.easy.util.StreamUtils.permute;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.board.Maze;
import de.amr.games.pacman.board.SpriteSheet;
import de.amr.games.pacman.board.Tile;
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
		maze = new Maze(app.getBoard(), getWidth(), getHeight() - 5 * Board.TILE_SIZE);

		redGhost = new Ghost(app.getBoard(), SpriteSheet.RED);
		pinkGhost = new Ghost(app.getBoard(), SpriteSheet.PINK);
		blueGhost = new Ghost(app.getBoard(), SpriteSheet.BLUE);
		orangeGhost = new Ghost(app.getBoard(), SpriteSheet.ORANGE);
		app.entities.add(redGhost, pinkGhost, blueGhost, orangeGhost);
		Stream.of(redGhost, pinkGhost, blueGhost, orangeGhost)
				.forEach(ghost -> findFreePosition().ifPresent(ghost::setMazePosition));
		
		pacMan = new PacMan(app.getBoard());
		app.entities.add(pacMan);
		pacMan.setMazePosition(14, 23);
		pacMan.enemies.addAll(Arrays.asList(redGhost, pinkGhost, blueGhost, orangeGhost));
		
		app.entities.all().forEach(GameEntity::init);
	}

	private Optional<Point> findFreePosition() {
		return permute(app.getBoard().positions().filter(p -> app.getBoard().getContent(p.x, p.y) == Tile.EMPTY)).findAny();
	}

	@Override
	public void update() {
		if (maze.getBoard().isMazeEmpty()) {
			maze.getBoard().resetContent();
		}
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			pacMan.setNextMoveDirection(Top4.W);
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			pacMan.setNextMoveDirection(Top4.E);
		} else if (Keyboard.keyDown(KeyEvent.VK_DOWN)) {
			pacMan.setNextMoveDirection(Top4.S);
		} else if (Keyboard.keyDown(KeyEvent.VK_UP)) {
			pacMan.setNextMoveDirection(Top4.N);
		}
		app.entities.all().forEach(GameEntity::update);
	}

	@Override
	public void draw(Graphics2D g) {
		// first three rows reserved for HUD
		g.translate(0, 3 * Board.TILE_SIZE);
		maze.draw(g);
		app.entities.all().forEach(e -> e.draw(g));
		g.translate(0, -3 * Board.TILE_SIZE);
		if (DEBUG) {
			drawGridLines(g);
		}
	}

	private void drawGridLines(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		for (int row = 0; row < maze.getBoard().numRows(); ++row) {
			g.drawLine(0, row * Board.TILE_SIZE, getWidth(), row * Board.TILE_SIZE);
		}
		for (int col = 0; col < maze.getBoard().numCols(); ++col) {
			g.drawLine(col * Board.TILE_SIZE, 0, col * Board.TILE_SIZE, getHeight());
		}
	}

}