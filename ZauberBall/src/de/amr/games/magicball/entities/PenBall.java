package de.amr.games.magicball.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;

public class PenBall extends Entity implements Lifecycle {

	private final BufferedImage canvas;
	private int thickness;
	private Color color;

	public PenBall(BufferedImage canvas) {
		this.canvas = canvas;
		thickness = 5;
		color = randomColor();
		tf.setWidth(thickness);
		tf.setHeight(thickness);
	}
	
	@Override
	public void init() {
	}

	@Override
	public void update() {
		tf.move();
		if (tf.getY() > canvas.getHeight() - tf.getHeight() || tf.getY() < 0) {
			tf.setVelocityY(-tf.getVelocityY());
		}
		if (tf.getX() < 0 || tf.getX() > canvas.getWidth() - tf.getWidth()) {
			tf.setVelocityX(-tf.getVelocityX());
		}
		color = randomColor();
		updateImage();
	}

	public void updateImage() {
		Graphics2D g = (Graphics2D) canvas.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.translate(tf.getX() + tf.getWidth() / 2, tf.getY() + tf.getHeight() / 2);
		g.fillOval(0, 0, thickness, thickness);
		g.translate(-tf.getX() - tf.getWidth() / 2, -tf.getY() - tf.getHeight() / 2);
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(canvas, 0, 0, null);
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