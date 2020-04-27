package de.amr.games.magicball.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.view.View;

public class PenBall extends Entity implements Lifecycle, View {

	private final BufferedImage canvas;
	private int thickness;
	private Color color;

	public PenBall(BufferedImage canvas) {
		this.canvas = canvas;
		thickness = 5;
		color = randomColor();
		tf.width = (thickness);
		tf.height = (thickness);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tf.move();
		if (tf.y > canvas.getHeight() - tf.height || tf.y < 0) {
			tf.vy *= -1;
		}
		if (tf.x < 0 || tf.x > canvas.getWidth() - tf.width) {
			tf.vx *= -1;
		}
		color = randomColor();
		updateImage();
	}

	public void updateImage() {
		Graphics2D g = (Graphics2D) canvas.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.translate(tf.x + tf.width / 2, tf.y + tf.height / 2);
		g.fillOval(0, 0, thickness, thickness);
		g.translate(-tf.x - tf.width / 2, -tf.y - tf.height / 2);
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