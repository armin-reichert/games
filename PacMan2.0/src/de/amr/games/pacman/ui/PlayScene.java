package de.amr.games.pacman.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.StartLevelEvent;
import de.amr.games.pacman.model.Game;

public class PlayScene extends ActiveScene<PacManApp> {

	private static boolean DEBUG = false;

	private final Game gameState;
	private final MazeUI maze;
	private final HUD hud;
	private final StatusDisplay status;
	private final GameController controller;

	public PlayScene(PacManApp app) {
		super(app);
		this.gameState = app.getGameState();

		hud = new HUD(gameState);
		hud.tf.moveTo(0, 0);

		maze = new MazeUI(gameState, getWidth(), getHeight() - 5 * PacManApp.TS);
		maze.tf.moveTo(0, 3 * PacManApp.TS);

		status = new StatusDisplay(gameState);
		status.tf.moveTo(0, getHeight() - 2 * PacManApp.TS);

		controller = new GameController(gameState, maze);
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
		for (int row = 1; row < gameState.maze.numRows(); ++row) {
			g.drawLine(0, row * PacManApp.TS, getWidth(), row * PacManApp.TS);
		}
		for (int col = 1; col < gameState.maze.numCols(); ++col) {
			g.drawLine(col * PacManApp.TS, 0, col * PacManApp.TS, getHeight());
		}
		g.setFont(new Font("Arial Narrow", Font.PLAIN, PacManApp.TS * 40 / 100));
		for (int row = 0; row < gameState.maze.numRows(); ++row) {
			for (int col = 0; col < gameState.maze.numCols(); ++col) {
				g.translate(col * PacManApp.TS, row * PacManApp.TS);
				g.drawString(String.format("%d,%d", col, row), PacManApp.TS / 8, PacManApp.TS / 2);
				g.translate(-col * PacManApp.TS, -row * PacManApp.TS);
			}
		}
	}
}