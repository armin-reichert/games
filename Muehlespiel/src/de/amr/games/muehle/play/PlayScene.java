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
 * The play scene of the mill game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<MillApp> implements MillGameUI {

	static final Color BOARD_COLOR = new Color(255, 255, 224);
	static final Color LINE_COLOR = Color.BLACK;

	private MillGameControl gameControl;

	private BoardUI boardUI;
	private final Stone[] stamp = new Stone[2];
	private TextArea messageArea;
	private AlienAssistant assistant;

	public PlayScene(MillApp app) {
		super(app);
	}

	@Override
	public void init() {
		setBgColor(BOARD_COLOR);

		Board board = new Board();
		boardUI = new BoardUI(board, 600, 600, BOARD_COLOR, LINE_COLOR);

		stamp[0] = new Stone(WHITE, boardUI.stoneRadius());
		stamp[1] = new Stone(BLACK, boardUI.stoneRadius());

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

		Player[] players = new Player[2];
		players[0] = whitePlayers[0];
		players[1] = blackPlayers[3];

		gameControl = new MillGameControl(board, players, this, app.pulse);
		assistant = new AlienAssistant(gameControl, this);
		assistant.setPlayers(players[0], players[1]);
		gameControl.setAssistant(assistant);

		Font msgFont = Assets.storeTrueTypeFont("message-font", "fonts/Cookie-Regular.ttf", Font.PLAIN, 36);
		messageArea = new TextArea();
		messageArea.setColor(Color.BLUE);
		messageArea.setFont(msgFont);

		// Layout
		boardUI.hCenter(getWidth());
		boardUI.tf.setY(50);
		messageArea.tf.moveTo(0, getHeight() - 90);
		assistant.hCenter(getWidth());
		assistant.tf.setY(getHeight() / 2 - 100);

		// control.setLogger(LOG);
		gameControl.init();
		assistant.init();
	}

	@Override
	public void update() {
		readInput();
		gameControl.update();
	}

	void readInput() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_CONTROL, KeyEvent.VK_N)) {
			gameControl.init();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
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
		if (gameControl.isPlacing()) {
			drawRemainingStonesCounter(g, 0, 40, getHeight() - 30);
			drawRemainingStonesCounter(g, 1, getWidth() - 100, getHeight() - 30);
		}
		if (gameControl.isRemoving() && gameControl.isInteractivePlayer(0) || gameControl.isInteractivePlayer(1)) {
			boardUI.markRemovableStones(g, gameControl.getPlayerNotInTurn().getColor());
		}
	}

	void drawRemainingStonesCounter(Graphics2D g, int i, int x, int y) {
		final int remaining = MillGameControl.NUM_STONES - gameControl.getNumStonesPlaced(i);
		final int inset = 6;
		g.translate(x + inset * remaining, y - inset * remaining);
		IntStream.range(0, remaining).forEach(j -> {
			stamp[i].draw(g);
			g.translate(-inset, inset);
		});
		if (remaining > 1) {
			g.setColor(gameControl.getTurn() == i ? Color.RED : Color.DARK_GRAY);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 2 * stamp[i].getRadius()));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(String.valueOf(remaining), 2 * stamp[i].getRadius(), stamp[i].getRadius());
		}
		g.translate(-x, -y);
	}
}