package de.amr.games.muehle;

import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;

import java.util.Locale;

import de.amr.easy.game.Application;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.game.impl.MillGameControl;
import de.amr.games.muehle.game.impl.MillGameScene;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.player.impl.InteractivePlayer;
import de.amr.games.muehle.player.impl.Zwick;

/**
 * Mühlespiel aka "Nine men's morris".
 * 
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class MillApp extends Application {

	public static void main(String[] args) {
		Messages.load((args.length > 0) ? new Locale(args[0]) : Locale.getDefault());
		launch(new MillApp());
	}

	private Board board;
	private Player whitePlayer;
	private Player blackPlayer;
	private MillGameControl game;

	public MillApp() {
		settings.title = Messages.text("title");
		settings.width = 800;
		settings.height = 800;
		settings.fullScreenMode = null;
	}

	@Override
	public void init() {
		pulse.setFrequency(20);
		board = new Board();
		whitePlayer = new InteractivePlayer(board, WHITE);
		blackPlayer = new Zwick(board, BLACK);
		game = new MillGameControl(board, whitePlayer, blackPlayer, pulse);
		MillGameScene scene = new MillGameScene(this);
		game.setUI(scene);
		selectView(scene);
	}

	public Board getBoard() {
		return board;
	}

	public Player getWhitePlayer() {
		return whitePlayer;
	}

	public Player getBlackPlayer() {
		return blackPlayer;
	}

	public MillGameControl getGame() {
		return game;
	}
}