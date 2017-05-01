package de.amr.games.perf;

import de.amr.easy.game.Application;

public class GamePerformanceApp extends Application {
	
	public static void main(String[] args) {
		GamePerformanceApp app = new GamePerformanceApp();
		app.settings.width = 1000;
		launch(app);
	}

	@Override
	protected void init() {
		views.add(new MainScene(this));
		views.show(MainScene.class);
		gameLoop.log = false;
	}
}
