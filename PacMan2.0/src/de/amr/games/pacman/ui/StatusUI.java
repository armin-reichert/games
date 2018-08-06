package de.amr.games.pacman.ui;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Game;

public class StatusUI extends GameEntity {

	private final Game gameState;
	private final Sprite s_pacMan;

	public StatusUI(Game gameState) {
		this.gameState = gameState;
		s_pacMan = new Sprite(Spritesheet.pacManWalking(Top4.W).frame(1)).scale(Spritesheet.TS * 2);
	}

	@Override
	public Sprite currentSprite() {
		return s_pacMan;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_pacMan);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		for (int i = 0; i < gameState.lives; ++i) {
			g.translate(i * s_pacMan.getWidth(), 0);
			s_pacMan.draw(g);
			g.translate(-i * s_pacMan.getWidth(), 0);
		}
		g.translate(-tf.getX(), -tf.getY());
	}
}