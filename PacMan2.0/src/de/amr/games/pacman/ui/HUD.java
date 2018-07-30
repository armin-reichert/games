package de.amr.games.pacman.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.Game;

public class HUD extends GameEntity {

	private final Game game;
	private Font font;

	public HUD(Game game) {
		this.game = game;
		font = Assets.storeTrueTypeFont("scoreFont", "arcadeclassic.ttf", Font.PLAIN, MazeUI.TS * 3 / 2);
	}

	@Override
	public Sprite currentSprite() {
		return null;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.empty();
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		g.setColor(Color.WHITE);
		g.setFont(font);
		g.drawString("SCORE", MazeUI.TS, MazeUI.TS);
		g.drawString(String.format("%06d", game.score), MazeUI.TS, MazeUI.TS * 2);
		g.drawString("LEVEL " + game.level, 20 * MazeUI.TS, MazeUI.TS);
		g.translate(-tf.getX(), -tf.getY());
	}
}