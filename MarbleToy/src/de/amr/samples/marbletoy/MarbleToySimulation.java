package de.amr.samples.marbletoy;

import java.awt.Color;

import de.amr.easy.game.Application;
import de.amr.easy.game.sprite.Sprite;
import de.amr.samples.marbletoy.entities.Marble;
import de.amr.samples.marbletoy.entities.MarbleToy;
import de.amr.samples.marbletoy.scenes.MainScene;

public class MarbleToySimulation extends Application {

	public static void main(String[] args) {
		MarbleToySimulation app = new MarbleToySimulation();
		app.settings.title = "Marble Toy State Machine";
		app.settings.width = 600;
		app.settings.height = 410;
		app.settings.bgColor = Color.WHITE;
		launch(app);
	}

	@Override
	public void init() {
		Marble marble = new Marble(new Sprite(assets.image("marble.png")).scale(50, 50));
		entities.add(new MarbleToy(new Sprite(assets.image("toy.png")), marble));
		views.add(new MainScene(this));
		views.show(MainScene.class);
	}
}
