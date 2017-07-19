package de.amr.samples.marbletoy;

import java.awt.Color;

import de.amr.easy.game.Application;
import de.amr.easy.game.sprite.Sprite;
import de.amr.samples.marbletoy.entities.Marble;
import de.amr.samples.marbletoy.entities.MarbleToy;
import de.amr.samples.marbletoy.scenes.MainScene;

public class MarbleToySimulation extends Application {

	public static void main(String[] args) {
		launch(new MarbleToySimulation());
	}

	public MarbleToySimulation() {
		settings.title = "Marble Toy State Machine";
		settings.width = 600;
		settings.height = 410;
		settings.bgColor = Color.WHITE;
	}

	@Override
	public void init() {
		Marble marble = new Marble(new Sprite(assets.image("marble.png")).scale(50, 50));
		entities.add(new MarbleToy(new Sprite(assets.image("toy.png")), marble));
		selectView(new MainScene(this));
	}
}