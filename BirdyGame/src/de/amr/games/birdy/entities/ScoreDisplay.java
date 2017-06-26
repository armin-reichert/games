package de.amr.games.birdy.entities;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.common.Score;
import de.amr.easy.game.entity.GameEntity;

public class ScoreDisplay extends GameEntity {

	private final Score score;
	private final float scale;
	private final Image[] digits;
	private String scoreText;

	public ScoreDisplay(Assets assets, Score score, float scale) {
		this.score = score;
		this.scale = scale;
		this.digits = new Image[10];
		for (int d = 0; d <= 9; d++) {
			BufferedImage digitImage = assets.image("number_score_0" + d);
			digits[d] = digitImage.getScaledInstance(-1, Math.round(scale) * digitImage.getHeight(), Image.SCALE_SMOOTH);
		}
		scoreText = String.valueOf(score.points);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		scoreText = String.valueOf(score.points);
	}

	@Override
	public void draw(Graphics2D g) {
		for (int i = 0; i < scoreText.length(); i++) {
			int digit = "0123456789".indexOf(scoreText.charAt(i));
			g.drawImage(digits[digit], (int) tr.getX() + i * (digits[0].getWidth(null) - Math.round(3 * scale)),
					(int) tr.getY(), null);
		}
	}

	@Override
	public int getWidth() {
		return scoreText.length() * digits[0].getWidth(null);
	}

	@Override
	public int getHeight() {
		return digits[0].getHeight(null);
	}
}
