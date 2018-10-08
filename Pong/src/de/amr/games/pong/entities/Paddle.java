package de.amr.games.pong.entities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;

public class Paddle extends Entity implements View {

	private final int upKey;
	private final int downKey;
	protected Dimension courtSize;
	protected int speed;
	protected Color color;
	protected Ball ball;

	public Paddle(int up, int down) {
		this.upKey = up;
		this.downKey = down;
	}

	public void setSize(int w, int h) {
		tf.setWidth(w);
		tf.setHeight(h);
	}

	public void setCourtSize(Dimension courtSize) {
		this.courtSize = courtSize;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setBall(Ball ball) {
		this.ball = ball;
	}

	@Override
	public void update() {
		if (Keyboard.keyDown(upKey)) {
			tf.setVelocityY(-speed);
		} else if (Keyboard.keyDown(downKey)) {
			tf.setVelocityY(speed);
		} else {
			tf.setVelocityY(0);
		}
		move();
	}

	protected void move() {
		tf.move();
		if (tf.getY() < 0) {
			tf.setY(0);
		} else if (tf.getY() >= courtSize.height - tf.getHeight()) {
			tf.setY(courtSize.height - tf.getHeight());
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);
		g.fillRect((int) tf.getX(), (int) tf.getY(), tf.getWidth(), tf.getHeight());
	}
}