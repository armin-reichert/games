package de.amr.games.pacman.ui;

import static de.amr.easy.game.Application.LOG;

import java.awt.Graphics2D;

import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GameActors;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Game;

public class GameUI implements ViewController {

	private final int width, height;
	private final GameController gameControl;
	private final MazeUI mazeUI;
	private final HUD hud;
	private final StatusUI statusUI;
	private final GameInfoUI gameInfoUI;

	public GameUI(int width, int height, Game game, GameActors actors) {
		this.width = width;
		this.height = height;

		mazeUI = new MazeUI(game.maze, actors);
		hud = new HUD(game);
		statusUI = new StatusUI(game);
		gameInfoUI = new GameInfoUI(game, actors, mazeUI);

		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * Spritesheet.TS);
		statusUI.tf.moveTo(0, (3 + game.maze.numRows()) * Spritesheet.TS);

		gameControl = new GameController(game, actors, mazeUI);
		gameControl.setLogger(LOG);
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