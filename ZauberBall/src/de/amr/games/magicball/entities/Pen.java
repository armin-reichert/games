package de.amr.games.magicball.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Pen extends GameEntity {

	private final BufferedImage image;
	private Color color;
	private int thickness;

	public Pen(BufferedImage image) {
		this.image = image;
		thickness = 20;
		color = randomColor();
	}

	@Override
	public int getWidth() {
		return thickness;
	}

	@Override
	public int getHeight() {
		return thickness;
	}

	@Override
	public void update() {
		tf.move();
		if (tf.getY() > image.getHeight() - getHeight() || tf.getY() < 0) {
			tf.setVelocityY(-tf.getVelocityY());
		}
		if (tf.getX() < 0 || tf.getX() > image.getWidth() - getWidth()) {
			tf.setVelocityX(-tf.getVelocityX());
		}
		color = randomColor();
		draw();
	}

	@Override
	public Sprite currentSprite() {
		return null;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.empty();
	}

	public void draw() {
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.translate(tf.getX() + getWidth() / 2, tf.getY() + getHeight() / 2);
		g.fillOval(0, 0, thickness, thickness);
		g.translate(-tf.getX() - getWidth() / 2, -tf.getY() - getHeight() / 2);
	}

	public void setSpeed(float vx, float vy) {
		tf.setVelocity(vx, vy);
	}

	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	private Color randomColor() {
		Random random = new Random();
		return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256), 255);
	}
}
