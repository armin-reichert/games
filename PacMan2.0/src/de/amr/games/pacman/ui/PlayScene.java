package de.amr.games.pacman.ui;

import java.awt.Graphics2D;

import de.amr.easy.game.scene.Scene;
import de.amr.easy.game.view.Controller;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.GameController;

public class PlayScene extends Scene<PacManApp> {

	private GameController controller;
	private MazeUI maze;
	private HUD hud;
	private StatusDisplay status;

	public PlayScene(GameController controller) {
		super(controller.app);
		this.controller = controller;
		hud = new HUD(controller.getGame());
		hud.tf.moveTo(0, 0);
		maze = new MazeUI(controller, getWidth(), getHeight() - 5 * PacManApp.TS);
		maze.tf.moveTo(0, 3 * PacManApp.TS);
		status = new StatusDisplay(controller.getGame());
		status.tf.moveTo(0, getHeight() - 2 * PacManApp.TS);
	}

	@Override
	public Controller getController() {
		return controller;
	}

	@Override
	public void init() {
		controller.init();
	}

	@Override
	public void draw(Graphics2D g) {
		hud.draw(g);
		maze.draw(g);
		status.draw(g);
	}
}