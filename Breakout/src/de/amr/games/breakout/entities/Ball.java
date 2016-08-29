package de.amr.games.breakout.entities;

import static de.amr.games.breakout.Globals.BALL_DIAMETER;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Ball extends GameEntity {

	private final int gameWidth;
	private final int gameHeight;

	public Ball(int gameWidth, int gameHeight) {
		this.gameWidth = gameWidth;
		this.gameHeight = gameHeight;
		setSprites(new Sprite("Balls/ball_green.png").scale(0, BALL_DIAMETER, BALL_DIAMETER));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tr.move();
		if (tr.getX() < 0) {
			tr.setX(0);
			if (tr.getVelX() < 0) {
				tr.setVelX(-tr.getVelX());
			}
		}
		if (tr.getX() > gameWidth - getWidth()) {
			tr.setX(gameWidth - getWidth());
			if (tr.getVelX() > 0) {
				tr.setVelX(-tr.getVelX());
			}
		}
		if (tr.getY() < 0) {
			tr.setY(0);
			if (tr.getVelY() < 0) {
				tr.setVelY(-tr.getVelY());
			}
		}
	}

	public boolean isOut() {
		return tr.getY() > gameHeight;
	}
}
