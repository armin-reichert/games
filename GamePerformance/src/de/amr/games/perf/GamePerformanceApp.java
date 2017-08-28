package de.amr.games.perf;

import de.amr.easy.game.Application;

public class GamePerformanceApp extends Application {

	public static void main(String[] args) {
		launch(new GamePerformanceApp());
	}

	public GamePerformanceApp() {
		settings.title = "Game performance measurement";
		settings.width = 1000;
		pulse.setLogger(LOG);
		pulse.setFrequency(50);
	}

	@Override
	public void init() {
		selectView(new MainScene(this));
	}
}
