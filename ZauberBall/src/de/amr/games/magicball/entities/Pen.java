package de.amr.games.magicball.entities;

import static de.amr.games.magicball.MagicBallApp.App;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Pen extends GameEntity {

	private final BufferedImage image;
	private Color color;
	private int thickness;

	public Pen(BufferedImage image) {
		super(new Sprite(App.assets.image("ball.png")).scale(0, 50, 50));
		this.image = image;
		thickness = 20;
		color = randomColor();
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tr.move();
		if (tr.getY() > image.getHeight() - getHeight() || tr.getY() < 0) {
			tr.setVelY(-tr.getVelY());
		}
		if (tr.getX() < 0 || tr.getX() > image.getWidth() - getWidth()) {
			tr.setVelX(-tr.getVelX());
		}
		color = randomColor();
		draw();
	}

	public void draw() {
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.translate(tr.getX() + getWidth() / 2, tr.getY() + getHeight() / 2);
		g.fillOval(0, 0, thickness, thickness);
		g.translate(-tr.getX() - getWidth() / 2, -tr.getY() - getHeight() / 2);
	}

	public void setSpeed(float vx, float vy) {
		tr.setVelocity(vx, vy);
	}

	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	private Color randomColor() {
		Random random = new Random();
		return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256), 255);
	}
}
