package de.amr.samples.marbletoy.scenes;

import static de.amr.samples.marbletoy.MarbleToySimulation.App;

import java.awt.Graphics2D;

import de.amr.easy.game.scene.Scene;
import de.amr.samples.marbletoy.MarbleToySimulation;
import de.amr.samples.marbletoy.entities.MarbleToy;

public class MainScene extends Scene<MarbleToySimulation> {

	private MarbleToy toy;

	public MainScene(MarbleToySimulation game) {
		super(game);
	}

	@Override
	public void init() {
		toy = App.entities.findAny(MarbleToy.class);
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
