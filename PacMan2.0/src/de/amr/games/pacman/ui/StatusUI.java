package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Spritesheet;

public class StatusUI extends GameEntity {

	private final Game gameState;
	private final Sprite spritePacMan;

	public StatusUI(Game gameState) {
		this.gameState = gameState;
		spritePacMan = new Sprite(Spritesheet.getPacManWalking(Top4.W)[1]).scale(TS * 2, TS * 2);
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