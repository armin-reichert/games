package de.amr.games.muehle;

import java.util.ResourceBundle;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.muehle.play.PlayScene;

/**
 * Mühlespiel aka "Nine men's morris".
 * 
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class MillApp extends Application {

	public final ResourceBundle messages;

	public static void main(String[] args) {
		launch(new MillApp());
	}

	public MillApp() {
		messages = ResourceBundle.getBundle("de.amr.games.muehle.Messages");
		settings.title = messages.getString("title");
		settings.width = 800;
		settings.height = 800;
		settings.fullScreenMode = FullScreen.Mode(1024, 768, 32);
		settings.set("seconds-per-move", 1f);
		pulse.setFrequency(25);
	}

	@Override
	public void init() {
		selectView(new PlayScene(this));
	}
}