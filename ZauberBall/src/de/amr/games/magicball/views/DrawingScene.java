package de.amr.games.magicball.views;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import de.amr.easy.game.view.ViewController;
import de.amr.games.magicball.MagicBallApp;
import de.amr.games.magicball.entities.Pen;

public class DrawingScene implements ViewController {

	private final MagicBallApp app;
	private final BufferedImage image;
	private final Pen pen;
	private final Pen pen2;

	public DrawingScene(MagicBallApp app) {
		this.app = app;
		image = new BufferedImage(app.settings.width, app.settings.height, BufferedImage.TYPE_INT_ARGB);
		pen = app.entities.store(new Pen(image));
		pen2 = app.entities.store(new Pen(image));
	}

	@Override
	public int getWidth() {
		return app.settings.width;
	}

	@Override
	public int getHeight() {
		return app.settings.height;
	}

	@Override
	public void init() {
		Random rnd = new Random();
		pen.setSpeed(5 + rnd.nextInt(5), 10 + rnd.nextInt(5));
		pen2.setSpeed(5 + rnd.nextInt(5), 10 + rnd.nextInt(5));
		pen.setThickness(10);
		pen2.setThickness(5);
	}

	@Override
	public void update() {
		pen.update();
		pen2.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(image, 0, 0, null);
		pen.draw(g);
		pen2.draw(g);
	}
}