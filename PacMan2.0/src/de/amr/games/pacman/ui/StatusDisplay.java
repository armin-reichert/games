package de.amr.games.pacman.ui;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.model.GameState;

public class StatusDisplay extends GameEntity {

	private final GameState gameState;
	private final Sprite spritePacMan;

	public StatusDisplay(GameState gameState) {
		this.gameState = gameState;
		spritePacMan = new Sprite(Spritesheet.getPacManWalking(Top4.W)[1]).scale(PacManApp.TS * 2, PacManApp.TS * 2);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		for (int i = 0; i < gameState.lives; ++i) {
			g.translate(i * spritePacMan.getWidth(), 0);
			spritePacMan.draw(g);
			g.translate(-i * spritePacMan.getWidth(), 0);
		}
		g.translate(-tf.getX(), -tf.getY());
	}
}