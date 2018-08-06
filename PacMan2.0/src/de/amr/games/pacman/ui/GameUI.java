package de.amr.games.pacman.ui;

import static de.amr.easy.game.Application.LOG;

import java.awt.Graphics2D;

import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.actor.GhostName;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Game;

public class GameUI implements ViewController {

	private final int width, height;
	private final GameController gameControl;
	private final Game game;
	private final MazeUI mazeUI;
	private final HUD hud;
	private final StatusUI statusUI;
	private final GameInfoUI gameInfoUI;

	public GameUI(PacManApp app) {
		this.width = app.settings.width;
		this.height = app.settings.height;
		this.game = new Game(app.maze, app.pulse::getFrequency);

		// UI
		mazeUI = new MazeUI(game);
		hud = new HUD(game);
		statusUI = new StatusUI(game);
		gameInfoUI = new GameInfoUI(game, mazeUI);

		// Layout
		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * Spritesheet.TS);
		statusUI.tf.moveTo(0, (3 + app.maze.numRows()) * Spritesheet.TS);

		gameControl = new GameController(game, mazeUI);
		gameControl.setLogger(LOG);
		mazeUI.getPacMan().getStateMachine().setLogger(LOG);
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.getStateMachine().setLogger(LOG));

		mazeUI.setGhostActive(GhostName.BLINKY, true);
		mazeUI.setGhostActive(GhostName.PINKY, false);
		mazeUI.setGhostActive(GhostName.INKY, false); // TODO
		mazeUI.setGhostActive(GhostName.CLYDE, false); // TODO
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void init() {
		gameControl.init();
	}

	@Override
	public void update() {
		gameControl.update();
		gameInfoUI.update();
	}

	@Override
	public void draw(Graphics2D g) {
		mazeUI.draw(g);
		gameInfoUI.draw(g);
		hud.draw(g);
		statusUI.draw(g);
	}
}