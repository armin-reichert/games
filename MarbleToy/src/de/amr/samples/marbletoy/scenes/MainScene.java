package de.amr.samples.marbletoy.scenes;

import java.awt.Graphics2D;

import de.amr.easy.game.scene.ActiveScene;
import de.amr.samples.marbletoy.MarbleToySimulation;
import de.amr.samples.marbletoy.entities.MarbleToy;

public class MainScene extends ActiveScene<MarbleToySimulation> {

	private MarbleToy toy;

	public MainScene(MarbleToySimulation game) {
		super(game);
	}

	@Override
	public void init() {
		toy = app.entities.findAny(MarbleToy.class);
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
