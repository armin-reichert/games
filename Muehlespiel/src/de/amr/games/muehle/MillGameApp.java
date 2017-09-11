package de.amr.games.muehle;

import static de.amr.games.muehle.model.board.StoneColor.BLACK;
import static de.amr.games.muehle.model.board.StoneColor.WHITE;

import java.util.Locale;

import de.amr.easy.game.Application;
import de.amr.games.muehle.controller.game.MillGameController;
import de.amr.games.muehle.controller.player.InteractivePlayer;
import de.amr.games.muehle.controller.player.Zwick;
import de.amr.games.muehle.model.MillGameModel;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.view.MillGameScene;

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

		MillGameController controller = new MillGameController(pulse, model);
		controller.setMoveTimeSeconds(0.75f);
		controller.setPlacingTimeSeconds(1.5f);

		MillGameScene gameScene = new MillGameScene(this, controller);
		controller.setView(gameScene);

		controller.setWhitePlayer(new InteractivePlayer(model, gameScene::findBoardPosition, WHITE));
		controller.setBlackPlayer(new Zwick(model, BLACK));

		selectView(gameScene);
	}
}