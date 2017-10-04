package de.amr.games.pacman2.play;

import de.amr.easy.game.Application;
import de.amr.games.pacman2.play.controller.PacmanGameController;
import de.amr.games.pacman2.play.model.PacmanGameData;
import de.amr.games.pacman2.play.view.PacmanGameScene;

public class PacmanGameApp extends Application {

	public static void main(String[] args) {
		launch(new PacmanGameApp());
	}

	public PacmanGameApp() {
		settings.title = "Armin's Pac-Man";
		settings.width = 448;
		settings.height = 576;
	}

	@Override
	public void init() {
		PacmanGameData model = new PacmanGameData();
		PacmanGameController controller = new PacmanGameController();
		PacmanGameScene view = new PacmanGameScene(this);

		controller.setModel(model);
		controller.setView(view);
		view.setController(controller);
		view.setModel(model);

		select(controller);
	}

}
