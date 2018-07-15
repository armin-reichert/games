package de.amr.games.pacman.ui;

import java.awt.Graphics2D;

import de.amr.easy.game.scene.ActiveScene;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Game;

public class PlayScene extends ActiveScene<PacManApp> {

	private Game game;
	private MazeUI maze;
	private HUD hud;
	private StatusDisplay status;
	private GameController controller;

	public PlayScene(PacManApp app) {
		super(app);
	}

	@Override
	public void init() {
		game = new Game();

		hud = new HUD(game);
		hud.tf.moveTo(0, 0);

		maze = new MazeUI(game, getWidth(), getHeight() - 5 * PacManApp.TS);
		maze.tf.moveTo(0, 3 * PacManApp.TS);

		status = new StatusDisplay(game);
		status.tf.moveTo(0, getHeight() - 2 * PacManApp.TS);

		controller = new GameController(game, maze);
		controller.init();
	}

	@Override
	public void update() {
		controller.update();
		maze.update();
	}

	@Override
	public void draw(Graphics2D g) {
		hud.draw(g);
		maze.draw(g);
		status.draw(g);
	}
}