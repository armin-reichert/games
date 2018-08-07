package de.amr.games.pacman.ui;

import java.awt.Graphics2D;

import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GameActors;
import de.amr.games.pacman.model.Game;

public class GameUI implements ViewController {

	public final int width, height;
	public final Game game;
	public final GameActors actors;
	public final MazeUI mazeUI;
	private final HUD hud;
	private final StatusUI statusUI;
	
	public GameUI(int width, int height, Game game, GameActors actors) {
		this.width = width;
		this.height = height;
		this.game = game;
		this.actors = actors;
		
		mazeUI = new MazeUI(game.maze, actors);
		hud = new HUD(game);
		statusUI = new StatusUI(game);

		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * Spritesheet.TS);
		statusUI.tf.moveTo(0, (3 + game.maze.numRows()) * Spritesheet.TS);
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
	}
	
	@Override
	public void update() {
	}
	
	@Override
	public View currentView() {
		return this;
	}

	@Override
	public void draw(Graphics2D g) {
		mazeUI.draw(g);
		hud.draw(g);
		statusUI.draw(g);
	}
}