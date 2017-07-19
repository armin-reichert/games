package de.amr.games.birdy.browser;

import de.amr.easy.game.Application;
import de.amr.games.birdy.utils.SpritesheetReader;

public class SpriteBrowser extends Application {

	public static void main(String[] args) {
		launch(new SpriteBrowser());
	}

	public SpriteBrowser() {
		settings.title = "Birdy Sprites";
		settings.width = 1024;
		settings.height = 1024;
		pulse.setFrequency(10);
	}

	@Override
	public void init() {
		SpritesheetReader.extractSpriteSheet(assets);
		selectView(new SpriteBrowserScene(this));
	}
}
