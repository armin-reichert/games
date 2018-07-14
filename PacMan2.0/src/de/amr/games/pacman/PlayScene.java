package de.amr.games.pacman;

import static de.amr.games.pacman.board.SpriteSheet.BLUE_GHOST;
import static de.amr.games.pacman.board.SpriteSheet.ORANGE_GHOST;
import static de.amr.games.pacman.board.SpriteSheet.PINK_GHOST;
import static de.amr.games.pacman.board.SpriteSheet.RED_GHOST;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.board.Maze;
import de.amr.games.pacman.control.PlayControl;
import de.amr.games.pacman.control.StartLevelEvent;
import de.amr.games.pacman.entities.Ghost;
import de.amr.games.pacman.entities.PacMan;

public class PlayScene extends ActiveScene<PacManApp> {

	private static boolean DEBUG = false;

	private final PlayControl playControl = new PlayControl(this);

	public final Board board;
	public final Maze maze;
	public final PacMan pacMan;
	public final Ghost[] ghosts = new Ghost[4];

	public PlayScene(PacManApp app) {
		super(app);
		this.board = app.board;
		maze = new Maze(board);
		maze.setSize(getWidth(), getHeight() - 5 * Board.TS);
		ghosts[RED_GHOST] = new Ghost(board, RED_GHOST);
		ghosts[PINK_GHOST] = new Ghost(board, PINK_GHOST);
		ghosts[BLUE_GHOST] = new Ghost(board, BLUE_GHOST);
		ghosts[ORANGE_GHOST] = new Ghost(board, ORANGE_GHOST);
		pacMan = new PacMan(board);
		pacMan.enemies.addAll(Arrays.asList(ghosts));
		app.entities.add(pacMan);
		app.entities.add(ghosts);

		pacMan.addObserver(playControl);
		Stream.of(ghosts).forEach(ghost -> ghost.addObserver(playControl));
	}

	@Override
	public void init() {
		playControl.dispatch(new StartLevelEvent(1));
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_D)) {
			DEBUG = !DEBUG;
		}
		playControl.update();
	}

	@Override
	public void draw(Graphics2D g) {
		drawHUD(g);
		g.translate(0, app.board.getFirstMazeRow() * Board.TS);
		maze.draw(g);
		app.entities.all().forEach(e -> e.draw(g));
		g.translate(0, -app.board.getFirstMazeRow() * Board.TS);
		drawStatus(g);
		if (DEBUG) {
			drawDebugInfo(g);
		}
	}

	private void drawStatus(Graphics2D g) {
	}

	private void drawHUD(Graphics2D g) {
	}

	private void drawDebugInfo(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		for (int row = 1; row < app.board.numRows(); ++row) {
			g.drawLine(0, row * Board.TS, getWidth(), row * Board.TS);
		}
		for (int col = 1; col < app.board.numCols(); ++col) {
			g.drawLine(col * Board.TS, 0, col * Board.TS, getHeight());
		}
		g.setFont(new Font("Arial Narrow", Font.PLAIN, Board.TS * 40 / 100));
		for (int row = 0; row < app.board.numRows(); ++row) {
			for (int col = 0; col < app.board.numCols(); ++col) {
				g.translate(col * Board.TS, row * Board.TS);
				g.drawString(String.format("%d,%d", col, row), Board.TS / 8, Board.TS / 2);
				g.translate(-col * Board.TS, -row * Board.TS);
			}
		}
	}
}