package de.amr.games.pong.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.controls.Score;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class ScoreDisplay extends GameEntity {

	private final Score scoreLeft;
	private final Score scoreRight;

	public ScoreDisplay(Score scoreLeft, Score scoreRight) {
		this.scoreLeft = scoreLeft;
		this.scoreRight = scoreRight;
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
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial Black", Font.PLAIN, 28));
		g.drawString("" + scoreLeft.points, (int) tf.getX() - 100, 50);
		g.drawString("" + scoreRight.points, (int) tf.getX() + 100, 50);
	}
}