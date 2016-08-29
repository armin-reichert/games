package de.amr.samples.marbletoy.scenes;

import java.awt.Graphics2D;

import de.amr.easy.game.Application;
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
		toy = Application.Entities.findAny(MarbleToy.class);
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
