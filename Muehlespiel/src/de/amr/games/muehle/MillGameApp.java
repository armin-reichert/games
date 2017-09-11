package de.amr.games.muehle;

import static de.amr.games.muehle.model.board.StoneColor.BLACK;
import static de.amr.games.muehle.model.board.StoneColor.WHITE;

import java.util.Locale;

import de.amr.easy.game.Application;
import de.amr.games.muehle.controller.game.MillGameController;
import de.amr.games.muehle.controller.player.InteractivePlayer;
import de.amr.games.muehle.controller.player.Zwick;
import de.amr.games.muehle.model.board.Board;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.view.MillGameScene;

/**
 * Mühlespiel aka "Nine men's morris".
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
		// Model
		Board board = new Board();

		// Controller
		MillGameController controller = new MillGameController(pulse, board);
		controller.setMoveTimeSeconds(0.75f);
		controller.setPlacingTimeSeconds(1.5f);

		// View
		MillGameScene gameScene = new MillGameScene(this, controller);
		controller.setView(gameScene);

		// Note: players should be created after connecting view with controller
		controller.setWhitePlayer(new InteractivePlayer(board, WHITE));
		controller.setBlackPlayer(new Zwick(board, BLACK));

		selectView(gameScene);
	}
}