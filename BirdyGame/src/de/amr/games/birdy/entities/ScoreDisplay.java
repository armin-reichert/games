package de.amr.games.birdy.entities;

import static java.lang.Math.round;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.view.View;
import de.amr.games.birdy.utils.Score;

/**
 * Displays the game score.
 * 
 * @author Armin Reichert
 */
public class ScoreDisplay extends Entity implements View {

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
			digits[d] = digitImage.getScaledInstance(-1, round(scale) * digitImage.getHeight(),
					Image.SCALE_SMOOTH);
		}
		update();
	}

	private String pointsText() {
		return String.format("%d", score.points);
	}

	@Override
	public void update() {
		scoreText = pointsText();
		tf.setWidth(scoreText.length() * digits[0].getWidth(null));
		tf.setHeight(digits[0].getHeight(null));
	}

	@Override
	public void draw(Graphics2D g) {
		for (int i = 0; i < scoreText.length(); i++) {
			int digit = "0123456789".indexOf(scoreText.charAt(i));
			g.drawImage(digits[digit],
					(int) tf.getX() + i * (digits[0].getWidth(null) - round(3 * scale)), (int) tf.getY(),
					null);
		}
	}
}