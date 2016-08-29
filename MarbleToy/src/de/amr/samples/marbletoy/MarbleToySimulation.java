package de.amr.samples.marbletoy;

import java.awt.Color;

import de.amr.easy.game.Application;
import de.amr.samples.marbletoy.entities.MarbleToy;
import de.amr.samples.marbletoy.scenes.MainScene;

public class MarbleToySimulation extends Application {

	public static void main(String[] args) {
		Settings.fps = 60;
		Settings.title = "Marble Toy State Machine";
		Settings.width = 600;
		Settings.height = 410;
		Settings.bgColor = Color.WHITE;
		launch(new MarbleToySimulation());
	}

	@Override
	protected void init() {
		Assets.image("toy.png");
		Assets.image("marble.png");
		Entities.add(new MarbleToy());
		Views.add(new MainScene(this));
		Views.show(MainScene.class);
	}
}
