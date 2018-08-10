package de.amr.games.pacman.ui;

import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.actor.GameActors;
import de.amr.games.pacman.model.Game;

public class GameUI implements ViewController {

	public static final Spritesheet SPRITES = new Spritesheet();

	protected final int width, height;
	protected final Game game;
	protected final GameActors actors;
	protected final MazeUI mazeUI;
	protected final HUD hud;
	protected final StatusUI statusUI;
	protected String infoText;
	protected Color infoTextColor;

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
		mazeUI.init();
		hud.init();
		statusUI.init();
	}

	@Override
	public void update() {
		mazeUI.update();
		hud.update();
		statusUI.update();
	}

	@Override
	public View currentView() {
		return this;
	}
	
	public void enableAnimation(boolean enable) {
		mazeUI.enableAnimation(enable);
	}
	
	public void setBonusTimer(int ticks) {
		mazeUI.setBonusTimer(ticks);
	}
	
	public void setMazeFlashing(boolean flashing) {
		mazeUI.setFlashing(flashing);
	}

	public void showInfo(String text, Color color) {
		infoText = text;
		infoTextColor = color;
	}

	public void hideInfo() {
		this.infoText = null;
	}

	private void drawInfoText(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setFont(Assets.font("scoreFont"));
		g2.setColor(infoTextColor);
		Rectangle box = g2.getFontMetrics().getStringBounds(infoText, g2).getBounds();
		g2.translate((width - box.width) / 2, (game.maze.infoTile.row + 1) * TS);
		g2.drawString(infoText, 0, 0);
		g2.dispose();
	}

	@Override
	public void draw(Graphics2D g) {
		mazeUI.draw(g);
		hud.draw(g);
		statusUI.draw(g);
		if (infoText != null) {
			g.translate(mazeUI.tf.getX(), mazeUI.tf.getY());
			drawInfoText(g);
			g.translate(-mazeUI.tf.getX(), -mazeUI.tf.getY());
		}
	}
}