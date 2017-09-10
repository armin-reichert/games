package de.amr.games.muehle.gameplay;

import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;

import java.util.Locale;

import de.amr.easy.game.Application;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.gameplay.ui.MillGameScene;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.InteractivePlayer;
import de.amr.games.muehle.player.Zwick;

/**
 * Mühlespiel aka "Nine men's morris".
 * 
 * @author Armin Reichert, Peter und Anna Schillo
 */
public class MillGameApp extends Application {

	public static void main(String[] args) {
		Messages.load((args.length > 0) ? new Locale(args[0]) : Locale.getDefault());
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

		Board board = new Board();

		MillGameController game = new MillGameController(pulse, board);
		game.setMoveTimeSeconds(0.75f);
		game.setPlacingTimeSeconds(1.5f);

		MillGameScene gameScene = new MillGameScene(this, game);
		game.setUI(gameScene);

		game.setWhitePlayer(new InteractivePlayer(board, WHITE));
		// game.setWhitePlayer(new Peter(board, WHITE));

		// game.setBlackPlayer(new InteractivePlayer(board, BLACK));
		game.setBlackPlayer(new Zwick(board, BLACK));

		selectView(gameScene);
	}
}