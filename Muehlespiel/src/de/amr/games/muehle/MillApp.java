package de.amr.games.muehle;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import de.amr.easy.game.Application;
import de.amr.games.muehle.play.PlayScene;

/**
 * Mühlespiel aka "Nine men's morris".
 * 
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class MillApp extends Application {

	public ResourceBundle messages;

	public static void main(String[] args) {
		Locale locale = (args.length > 0) ? new Locale(args[0]) : Locale.getDefault();
		launch(new MillApp(locale));
	}

	public MillApp(Locale locale) {
		messages = ResourceBundle.getBundle("de.amr.games.muehle.Messages", locale);
		settings.title = messages.getString("title");
		settings.width = 800;
		settings.height = 800;
		settings.fullScreenMode = null;
		pulse.setFrequency(25);
	}

	@Override
	public void init() {
		selectView(new PlayScene(this));
	}

	public String msg(String key, Object... args) {
		try {
			return MessageFormat.format(messages.getString(key), args);
		} catch (MissingResourceException e) {
			return "MISSING RESOURCE: " + key;
		}
	}
}