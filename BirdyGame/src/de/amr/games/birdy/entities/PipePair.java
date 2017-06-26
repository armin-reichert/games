package de.amr.games.birdy.entities;

import static de.amr.games.birdy.BirdyGame.OBSTACLE_PASSAGE_HEIGHT;
import static de.amr.games.birdy.BirdyGame.OBSTACLE_PIPE_HEIGHT;
import static de.amr.games.birdy.BirdyGame.WORLD_SPEED;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.birdy.BirdyGame;

public class PipePair {

	private final GameEntity pipeDown;
	private final GameEntity pipeUp;
	private final GameEntity passage;
	private boolean lighted;

	private class Passage extends GameEntity {

		@Override
		public void init() {
		}

		@Override
		public void update() {
			tr.move();
		}

		@Override
		public int getWidth() {
			return pipeDown.getWidth();
		}

		@Override
		public int getHeight() {
			return OBSTACLE_PASSAGE_HEIGHT;
		}

		@Override
		public void draw(Graphics2D g) {
			g.translate(tr.getX(), tr.getY());
			if (lighted) {
				g.setColor(new Color(255, 255, 0, new Random().nextInt(170)));
				g.fillRect(3, 0, getWidth() - 6, OBSTACLE_PASSAGE_HEIGHT);
			}
			g.translate(-tr.getX(), -tr.getY());
		}
	}

	public PipePair(BirdyGame app, int centerY) {
		pipeDown = app.entities.add(new PipeDown(app.assets));
		pipeDown.tr.setY(centerY - OBSTACLE_PASSAGE_HEIGHT / 2 - OBSTACLE_PIPE_HEIGHT);
		passage = new Passage();
		passage.tr.setY(centerY - OBSTACLE_PASSAGE_HEIGHT / 2);
		pipeUp = app.entities.add(new PipeUp(app.assets));
		pipeUp.tr.setY(centerY + OBSTACLE_PASSAGE_HEIGHT / 2);
		setVelocityX(WORLD_SPEED);
	}

	public void update() {
		pipeDown.update();
		passage.update();
		pipeUp.update();
	}

	public void render(Graphics2D g) {
		pipeDown.draw(g);
		passage.draw(g);
		pipeUp.draw(g);
	}

	public void setVelocityX(float x) {
		pipeDown.tr.setVelX(x);
		passage.tr.setVelX(x);
		pipeUp.tr.setVelX(x);
	}

	public void setPositionX(float x) {
		pipeDown.tr.setX(x);
		passage.tr.setX(x);
		pipeUp.tr.setX(x);
	}

	public float getPositionX() {
		return pipeDown.tr.getX();
	}

	public void setLighted(boolean lighted) {
		this.lighted = lighted;
	}

	public int getHeight() {
		return pipeDown.getHeight() + passage.getHeight() + pipeUp.getHeight();
	}

	public int getWidth() {
		return Math.max(pipeDown.getWidth(), pipeUp.getWidth());
	}

	public GameEntity getPipeDown() {
		return pipeDown;
	}

	public GameEntity getPipeUp() {
		return pipeUp;
	}

	public GameEntity getPassage() {
		return passage;
	}
}