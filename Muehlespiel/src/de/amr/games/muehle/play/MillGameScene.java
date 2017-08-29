package de.amr.games.muehle.play;

import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.stream.IntStream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.common.TextArea;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.scene.Scene;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.player.impl.InteractivePlayer;
import de.amr.games.muehle.player.impl.Peter;
import de.amr.games.muehle.player.impl.RandomPlayer;
import de.amr.games.muehle.player.impl.Zwick;
import de.amr.games.muehle.ui.BoardUI;
import de.amr.games.muehle.ui.Stone;

/**
 * The scene (UI) of the mill game.
 * 
 * @author Armin Reichert
 */
public class MillGameScene extends Scene<MillApp> implements MillGameUI {

	private MillGameControl game;
	private BoardUI boardUI;
	private TextArea messageArea;
	private Assistant assistant;

	public MillGameScene(MillApp app) {
		super(app);
	}

	@Override
	public void init() {
		setBgColor(BOARD_COLOR.darker());

		Board board = new Board();

		boardUI = new BoardUI(board, getWidth() * 3 / 4, getHeight() * 3 / 4, BOARD_COLOR, LINE_COLOR);

		/*@formatter:off*/
		Player[] whitePlayers = { 
				new InteractivePlayer(board, WHITE, boardUI::findPosition),
				new RandomPlayer(board, WHITE), 
				new Peter(board, WHITE),
				new Zwick(board, WHITE)
		};

		Player[] blackPlayers = { 
				new InteractivePlayer(board, BLACK, boardUI::findPosition),
				new RandomPlayer(board, BLACK), 
				new Peter(board, BLACK),
				new Zwick(board, BLACK)
		};
		/*@formatter:on*/

		Player whitePlayer = whitePlayers[0];
		Player blackPlayer = blackPlayers[3];

		game = new MillGameControl(board, whitePlayer, blackPlayer, this, app.pulse);
		assistant = new Assistant(game, whitePlayer, blackPlayer, this);
		game.setAssistant(assistant);

		messageArea = new TextArea();
		messageArea.setColor(Color.BLUE);
		messageArea.setFont(Assets.storeTrueTypeFont("message-font", "fonts/Cookie-Regular.ttf", Font.PLAIN, 36));

		// Layout
		boardUI.hCenter(getWidth());
		boardUI.tf.setY(50);
		messageArea.tf.moveTo(0, getHeight() - 90);
		assistant.hCenter(getWidth());
		assistant.tf.setY(getHeight() / 2 - 100);

		// control.setLogger(LOG);
		game.init();
	}

	@Override
	public void update() {
		readInput();
		game.update();
	}

	void readInput() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
			assistant.toggle();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			assistant.setEnabled(true);
			assistant.setAssistanceLevel(0);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			assistant.setEnabled(true);
			assistant.setAssistanceLevel(1);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			boardUI.togglePositionNumbers();
		}
	}

	@Override
	public void clearBoard() {
		boardUI.clear();
	}

	@Override
	public Optional<Stone> getStoneAt(int p) {
		return boardUI.stoneAt(p);
	}

	@Override
	public void removeStoneAt(int p) {
		boardUI.removeStoneAt(p);
	}

	@Override
	public void putStoneAt(int p, StoneColor color) {
		boardUI.putStoneAt(p, color);
	}

	@Override
	public void moveStone(int from, int to) {
		boardUI.moveStone(from, to);
	}

	@Override
	public void showMessage(String key, Object... args) {
		messageArea.setText(Messages.text(key, args));
	}

	@Override
	public Vector2f centerPoint(int p) {
		return boardUI.centerPoint(p);
	}

	@Override
	public void markPosition(Graphics2D g, int p, Color color) {
		boardUI.markPosition(g, p, color);
	}

	@Override
	public void markPositions(Graphics2D g, IntStream positions, Color color) {
		boardUI.markPositions(g, positions, color);
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		boardUI.draw(g);
		assistant.draw(g);
		messageArea.hCenter(getWidth());
		messageArea.draw(g);
		if (game.isPlacing()) {
			drawRemainingStonesCounter(g, 0, 40, getHeight() - 30);
			drawRemainingStonesCounter(g, 1, getWidth() - 100, getHeight() - 30);
		}
		if (game.isRemoving() && game.isInteractivePlayer(0) || game.isInteractivePlayer(1)) {
			boardUI.markRemovableStones(g, game.getPlayerNotInTurn().getColor());
		}
	}

	void drawRemainingStonesCounter(Graphics2D g, int i, int x, int y) {
		final Stone stamp = new Stone(i == 0 ? WHITE : BLACK, boardUI.getStoneRadius());
		final int remaining = MillGame.NUM_STONES - game.getNumStonesPlaced(i);
		final int inset = 6;
		g.translate(x + inset * remaining, y - inset * remaining);
		IntStream.range(0, remaining).forEach(j -> {
			stamp.draw(g);
			g.translate(-inset, inset);
		});
		if (remaining > 1) {
			g.setColor(game.getTurn() == i ? Color.RED : Color.DARK_GRAY);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 2 * stamp.getRadius()));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(String.valueOf(remaining), 2 * stamp.getRadius(), stamp.getRadius());
		}
		g.translate(-x, -y);
	}
}