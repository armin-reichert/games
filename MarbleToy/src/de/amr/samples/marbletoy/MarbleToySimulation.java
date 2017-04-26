package de.amr.samples.marbletoy;

import java.awt.Color;

import de.amr.easy.game.Application;
import de.amr.samples.marbletoy.entities.MarbleToy;
import de.amr.samples.marbletoy.scenes.MainScene;

public class MarbleToySimulation extends Application {
	
	public static final MarbleToySimulation App = new MarbleToySimulation();

	public static void main(String[] args) {
		App.settings.fps = 60;
		App.settings.title = "Marble Toy State Machine";
		App.settings.width = 600;
		App.settings.height = 410;
		App.settings.bgColor = Color.WHITE;
		launch(App);
	}

	@Override
	protected void init() {
		assets.image("toy.png");
		assets.image("marble.png");
		entities.add(new MarbleToy());
		views.add(new MainScene(this));
		views.show(MainScene.class);
	}
}
