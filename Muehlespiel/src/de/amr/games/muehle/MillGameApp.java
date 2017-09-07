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
 * @author Armin Reichert, Peter und Anna Schillo
 */
public class MillGameApp extends Application {

	public static void main(String[] args) {
		Messages.load((args.length > 0) ? new Locale(args[0]) : Locale.getDefault());
		launch(new MillGameApp());
	}

	private Board board;
	private MillGameScene gameScene;
	private Player whitePlayer;
	private Player blackPlayer;
	private MillGameControl game;

	public MillGameApp() {
		settings.title = Messages.text("title");
		settings.width = 800;
		settings.height = 800;
		settings.fullScreenMode = null;
		pulse.setFrequency(25);
	}

	@Override
	public void init() {

		board = new Board();

		game = new MillGameControl(this);
		game.setMoveTimeSeconds(0.75f);
		game.setPlacingTimeSeconds(1.5f);

		gameScene = new MillGameScene(this);
		game.setUI(gameScene);

		setWhitePlayer(new InteractivePlayer(board, WHITE));
		// setWhitePlayer(new Peter(board, WHITE));

		// setBlackPlayer(new InteractivePlayer(board, BLACK));
		setBlackPlayer(new Zwick(board, BLACK));

		selectView(gameScene);
	}

	public Board getBoard() {
		return board;
	}

	public Player getWhitePlayer() {
		return whitePlayer;
	}

	public void setWhitePlayer(Player whitePlayer) {
		this.whitePlayer = whitePlayer;
		gameScene.playerChanged(whitePlayer);
	}

	public Player getBlackPlayer() {
		return blackPlayer;
	}

	public void setBlackPlayer(Player blackPlayer) {
		this.blackPlayer = blackPlayer;
		gameScene.playerChanged(blackPlayer);
	}

	public MillGameControl getGame() {
		return game;
	}
}