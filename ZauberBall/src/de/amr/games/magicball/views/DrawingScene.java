package de.amr.games.magicball.views;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.amr.easy.game.view.Lifecycle;
import de.amr.easy.game.view.View;
import de.amr.games.magicball.entities.PenBall;

public class DrawingScene implements View, Lifecycle {

	private final BufferedImage canvas;
	private final List<PenBall> balls = new ArrayList<>();

	public DrawingScene(int width, int height, int nBalls) {
		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < nBalls; ++i) {
			balls.add(new PenBall(canvas));
		}
	}

	@Override
	public void init() {
		Random rnd = new Random();
		for (PenBall ball : balls) {
			ball.setSpeed(4 + rnd.nextInt(6), 6 + rnd.nextInt(14));
			ball.setThickness(3 + rnd.nextInt(12));
		}
	}

	@Override
	public void update() {
		balls.forEach(PenBall::update);
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(canvas, 0, 0, null);
		balls.forEach(ball -> ball.draw(g));
	}
}