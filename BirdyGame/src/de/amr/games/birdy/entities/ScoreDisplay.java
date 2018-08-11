package de.amr.games.birdy.entities;

import static java.lang.Math.round;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controls.Score;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

/**
 * Displays the game score.
 * 
 * @author Armin Reichert
 */
public class ScoreDisplay extends GameEntity {

	private final Score score;
	private final float scale;
	private final Image[] digits;
	private String scoreText;

	public ScoreDisplay(Score score, float scale) {
		this.score = score;
		this.scale = scale;
		this.digits = new Image[10];
		for (int d = 0; d <= 9; d++) {
			BufferedImage digitImage = Assets.image("number_score_0" + d);
			digits[d] = digitImage.getScaledInstance(-1, round(scale) * digitImage.getHeight(), Image.SCALE_SMOOTH);
		}
		scoreText = pointsText();
	}

	@Override
	public int getWidth() {
		return scoreText.length() * digits[0].getWidth(null);
	}

	@Override
	public int getHeight() {
		return digits[0].getHeight(null);
	}

	private String pointsText() {
		return String.format("%d", score.points);
	}

	@Override
	public void update() {
		scoreText = pointsText();
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
		for (int i = 0; i < scoreText.length(); i++) {
			int digit = "0123456789".indexOf(scoreText.charAt(i));
			g.drawImage(digits[digit], (int) tf.getX() + i * (digits[0].getWidth(null) - round(3 * scale)), (int) tf.getY(),
					null);
		}
	}
}