package de.amr.games.muehle;

import java.util.Locale;

import de.amr.easy.game.Application;
import de.amr.games.muehle.controller.game.MillGameController;
import de.amr.games.muehle.model.MillGameModel;
import de.amr.games.muehle.msg.Messages;

/**
 * Mühle (engl. "Nine men's morris").
 * 
 * @author Armin Reichert, Peter und Anna Schillo
 */
public class MillGameApp extends Application {

	public static void main(String... args) {
		Messages.load(args.length > 0 ? new Locale(args[0]) : Locale.getDefault());
		launch(new MillGameApp());
	}

	public MillGameApp() {
		settings.title = Messages.text("title");
		settings.width = 800;
		settings.height = 800;
		settings.fullScreenMode = null;
		pulse.setFrequency(25);
	}

	@Override
	public void init() {
		MillGameModel model = new MillGameModel();

		MillGameController controller = new MillGameController(this, pulse, model);
		controller.setMoveTimeSeconds(0.75f);
		controller.setPlacingTimeSeconds(1.5f);
		controller.setLogger(Application.LOG);
		setController(controller);
	}
}