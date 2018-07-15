package de.amr.games.pacman.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.model.Game;

public class HUD extends GameEntity {

	private final Game game;
	private Font font;

	public HUD(Game game) {
		this.game = game;
		font = Assets.storeTrueTypeFont("scoreFont", "arcadeclassic.ttf", Font.PLAIN, PacManApp.TS * 3/2);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		g.setColor(Color.WHITE);
		g.setFont(font);
		g.drawString("SCORE", PacManApp.TS, PacManApp.TS);
		g.drawString(String.format("%06d", game.score), PacManApp.TS, PacManApp.TS * 2);
		g.drawString("LEVEL " + game.level, 20*PacManApp.TS, PacManApp.TS);
		g.translate(-tf.getX(), -tf.getY());
	}

}
