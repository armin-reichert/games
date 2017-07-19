package de.amr.games.birdy;

import de.amr.easy.game.Application;
import de.amr.games.birdy.scenes.browser.SpriteBrowserScene;
import de.amr.games.birdy.tools.SpritesheetReader;

public class BirdySpriteBrowser extends Application {

	public static void main(String[] args) {
		launch(new BirdySpriteBrowser());
	}

	public BirdySpriteBrowser() {
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
