package de.amr.games.pacman;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.games.pacman.board.MazeContent;
import de.amr.games.pacman.control.PlayControl;
import de.amr.games.pacman.control.StartLevelEvent;
import de.amr.games.pacman.entities.HUD;
import de.amr.games.pacman.entities.Maze;
import de.amr.games.pacman.entities.StatusDisplay;

public class PlayScene extends ActiveScene<PacManApp> {

	private static boolean DEBUG = false;

	private final GameState gameState;
	private final Maze maze;
	private final HUD hud;
	private final StatusDisplay status;
	private final PlayControl controller;

	public PlayScene(PacManApp app) {
		super(app);
		this.gameState = app.getGameState();

		hud = new HUD();
		hud.tf.moveTo(0, 0);

		maze = new Maze(gameState.mazeContent, getWidth(), getHeight() - 5 * MazeContent.TS);
		maze.tf.moveTo(0, 3 * MazeContent.TS);

		status = new StatusDisplay();
		status.tf.moveTo(0, getHeight() - 2 * MazeContent.TS);

		controller = new PlayControl(gameState, maze);
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
		for (int row = 1; row < gameState.mazeContent.numRows(); ++row) {
			g.drawLine(0, row * MazeContent.TS, getWidth(), row * MazeContent.TS);
		}
		for (int col = 1; col < gameState.mazeContent.numCols(); ++col) {
			g.drawLine(col * MazeContent.TS, 0, col * MazeContent.TS, getHeight());
		}
		g.setFont(new Font("Arial Narrow", Font.PLAIN, MazeContent.TS * 40 / 100));
		for (int row = 0; row < gameState.mazeContent.numRows(); ++row) {
			for (int col = 0; col < gameState.mazeContent.numCols(); ++col) {
				g.translate(col * MazeContent.TS, row * MazeContent.TS);
				g.drawString(String.format("%d,%d", col, row), MazeContent.TS / 8, MazeContent.TS / 2);
				g.translate(-col * MazeContent.TS, -row * MazeContent.TS);
			}
		}
	}
}