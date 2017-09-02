package de.amr.games.muehle;

import java.util.Locale;

import de.amr.easy.game.Application;
import de.amr.games.muehle.game.impl.MillGameScene;
import de.amr.games.muehle.msg.Messages;

/**
 * Mühlespiel aka "Nine men's morris".
 * 
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class MillApp extends Application {

	public static void main(String[] args) {
		Locale locale = (args.length > 0) ? new Locale(args[0]) : Locale.getDefault();
		Messages.load(locale);
		launch(new MillApp());
	}

	public MillApp() {
		settings.title = Messages.text("title");
		settings.width = 800;
		settings.height = 800;
		settings.fullScreenMode = null;
		pulse.setFrequency(20);
	}

	@Override
	public void init() {
		selectView(new MillGameScene(this));
	}
}