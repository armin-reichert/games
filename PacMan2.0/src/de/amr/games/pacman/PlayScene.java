package de.amr.games.pacman;

import static de.amr.easy.util.StreamUtils.permute;
import static de.amr.games.pacman.board.SpriteSheet.BLUE_GHOST;
import static de.amr.games.pacman.board.SpriteSheet.ORANGE_GHOST;
import static de.amr.games.pacman.board.SpriteSheet.PINK_GHOST;
import static de.amr.games.pacman.board.SpriteSheet.RED_GHOST;

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
import de.amr.games.pacman.board.Tile;
import de.amr.games.pacman.entities.Ghost;
import de.amr.games.pacman.entities.PacMan;

public class PlayScene extends ActiveScene<PacManApp> {

	private static boolean DEBUG = false;

	private Maze maze;
	private PacMan pacMan;
	private Ghost[] ghosts = new Ghost[4];

	public PlayScene(PacManApp app) {
		super(app);
		maze = new Maze(app.board);
		maze.setSize(getWidth(), getHeight() - 5 * Board.TILE_SIZE);
		ghosts[RED_GHOST] = new Ghost(app.board, RED_GHOST);
		ghosts[PINK_GHOST] = new Ghost(app.board, PINK_GHOST);
		ghosts[BLUE_GHOST] = new Ghost(app.board, BLUE_GHOST);
		ghosts[ORANGE_GHOST] = new Ghost(app.board, ORANGE_GHOST);
		pacMan = new PacMan(app.board);
		pacMan.enemies.addAll(Arrays.asList(ghosts));
		app.entities.add(pacMan);
		app.entities.add(ghosts);
	}

	@Override
	public void init() {
		Stream.of(ghosts).forEach(ghost -> findFreePosition().ifPresent(ghost::setMazePosition));
		pacMan.setMazePosition(14, 23);
		app.entities.all().forEach(GameEntity::init);
	}

	@Override
	public void update() {
		if (app.board.isMazeEmpty()) {
			app.board.resetContent();
			init();
			return;
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
		// first board rows are reserved for HUD
		g.translate(0, app.board.getFirstMazeRow() * Board.TILE_SIZE);
		maze.draw(g);
		app.entities.all().forEach(e -> e.draw(g));
		g.translate(0, -app.board.getFirstMazeRow() * Board.TILE_SIZE);
		if (DEBUG) {
			drawGridLines(g);
		}
	}

	private Optional<Point> findFreePosition() {
		return permute(app.board.positions().filter(p -> app.board.getContent(p.x, p.y) == Tile.EMPTY)).findAny();
	}

	private void drawGridLines(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		for (int row = 1; row < app.board.numRows(); ++row) {
			g.drawLine(0, row * Board.TILE_SIZE, getWidth(), row * Board.TILE_SIZE);
		}
		for (int col = 1; col < app.board.numCols(); ++col) {
			g.drawLine(col * Board.TILE_SIZE, 0, col * Board.TILE_SIZE, getHeight());
		}
	}
}