package de.amr.samples.marbletoy.scenes;

import java.awt.Graphics2D;

import de.amr.easy.game.view.ViewController;
import de.amr.samples.marbletoy.MarbleToySimulation;
import de.amr.samples.marbletoy.entities.MarbleToy;

public class MainScene implements ViewController {

	private MarbleToySimulation app;
	private MarbleToy toy;

	public MainScene(MarbleToySimulation app) {
		this.app = app;
	}

	public int getWidth() {
		return app.settings.width;
	}

	public int getHeight() {
		return app.settings.height;
	}

	@Override
	public void init() {
		toy = app.entities.ofClass(MarbleToy.class).findFirst().get();
		toy.init();
	}

	@Override
	public void update() {
		toy.update();
	}

	@Override
	public void draw(Graphics2D g) {
		toy.draw(g);
	}
}
