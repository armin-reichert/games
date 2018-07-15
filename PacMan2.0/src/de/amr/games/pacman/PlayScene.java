package de.amr.games.pacman;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.control.PlayControl;
import de.amr.games.pacman.control.StartLevelEvent;
import de.amr.games.pacman.entities.HUD;
import de.amr.games.pacman.entities.Maze;
import de.amr.games.pacman.entities.StatusDisplay;

public class PlayScene extends ActiveScene<PacManApp> {

	private static boolean DEBUG = false;

	private final Maze maze;
	private final HUD hud;
	private final StatusDisplay status;
	private final PlayControl controller;

	public PlayScene(PacManApp app) {
		super(app);

		hud = new HUD();
		hud.tf.moveTo(0, 0);

		maze = new Maze(app.board, getWidth(), getHeight() - 5 * Board.TS);
		maze.tf.moveTo(0, 3 * Board.TS);

		status = new StatusDisplay();
		status.tf.moveTo(0, getHeight() - 2 * Board.TS);

		controller = new PlayControl(app.board, maze.pacMan, maze.ghosts);
		maze.pacMan.addObserver(controller);
		Stream.of(maze.ghosts).forEach(ghost -> ghost.addObserver(controller));
	}

	@Override
	public void init() {
		controller.dispatch(new StartLevelEvent(1));
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_D)) {
			DEBUG = !DEBUG;
		}
		controller.update();
	}

	@Override
	public void draw(Graphics2D g) {
		hud.draw(g);
		maze.draw(g);
		status.draw(g);
		if (DEBUG) {
			drawDebugInfo(g);
		}
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