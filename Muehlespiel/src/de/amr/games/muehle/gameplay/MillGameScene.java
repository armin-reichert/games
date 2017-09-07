package de.amr.games.muehle.gameplay;

import static de.amr.games.muehle.gameplay.MillGame.NUM_STONES;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Optional;
import java.util.stream.IntStream;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.common.TextArea;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.scene.Scene;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.InteractivePlayer;
import de.amr.games.muehle.player.Player;
import de.amr.games.muehle.ui.BoardUI;
import de.amr.games.muehle.ui.Stone;

/**
 * The scene (UI) of the mill game.
 * 
 * @author Armin Reichert
 */
public class MillGameScene extends Scene<MillGameApp> implements MillGameUI {

	private final BoardUI boardUI;
	private final TextArea messageArea;
	private final Assistant assistant;
	private Stone stoneTemplate;
	private Font stonesCounterFont;

	public MillGameScene(MillGameApp app) {
		super(app);
		setBgColor(BOARD_COLOR.darker());
		boardUI = new BoardUI(app.getBoard());
		messageArea = new TextArea();
		assistant = new Assistant(app.getGame(), this);
	}

	@Override
	public void init() {

		boardUI.setSize(getWidth() * 3 / 4);
		boardUI.setBgColor(BOARD_COLOR);
		boardUI.setLineColor(LINE_COLOR);
		boardUI.hCenter(getWidth());
		boardUI.tf.setY(50);

		if (app.getWhitePlayer() instanceof InteractivePlayer) {
			InteractivePlayer ip = (InteractivePlayer) app.getWhitePlayer();
			ip.setBoardPositionFinder(boardUI::findPosition);
		}

		if (app.getBlackPlayer() instanceof InteractivePlayer) {
			InteractivePlayer ip = (InteractivePlayer) app.getBlackPlayer();
			ip.setBoardPositionFinder(boardUI::findPosition);
		}

		stoneTemplate = new Stone(StoneColor.WHITE, boardUI.getStoneRadius());
		stonesCounterFont = new Font(Font.MONOSPACED, Font.BOLD, 2 * boardUI.getStoneRadius());

		messageArea.setColor(Color.BLUE);
		messageArea.setFont(Assets.storeTrueTypeFont("message-font", "fonts/Cookie-Regular.ttf", Font.PLAIN, 36));
		messageArea.tf.moveTo(0, getHeight() - 90);

		assistant.hCenter(getWidth());
		assistant.tf.setY(getHeight() / 2 - 100);

		app.getGame().setAssistant(assistant);
		app.getGame().setLogger(Application.LOG);
		app.getGame().init();
	}

	@Override
	public void update() {
		app.getGame().update();
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
	public Vector2f getLocation(int p) {
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
	public void toggleBoardPositionNumbers() {
		boardUI.togglePositionNumbers();
	}

	@Override
	public void playerChanged(Player player) {
		if (player instanceof InteractivePlayer) {
			((InteractivePlayer) player).setBoardPositionFinder(boardUI::findPosition);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		boardUI.draw(g);
		assistant.draw(g);
		messageArea.hCenter(getWidth());
		messageArea.draw(g);
		if (app.getGame().isPlacing()) {
			drawRemainingStonesCounter(g, app.getGame().getWhitePlayer(), NUM_STONES - app.getGame().numWhiteStonesPlaced(),
					40, getHeight() - 30);
			drawRemainingStonesCounter(g, app.getGame().getBlackPlayer(), NUM_STONES - app.getGame().numBlackStonesPlaced(),
					getWidth() - 100, getHeight() - 30);
		}
		if (app.getGame().isRemoving() && app.getGame().getPlayerInTurn().isInteractive()) {
			boardUI.markRemovableStones(g, app.getGame().getPlayerNotInTurn().getColor());
		}
	}

	private void drawRemainingStonesCounter(Graphics2D g, Player player, int stonesLeft, int x, int y) {
		stoneTemplate.setColor(player.getColor());
		final int inset = 6;
		g.translate(x + inset * stonesLeft, y - inset * stonesLeft);
		IntStream.range(0, stonesLeft).forEach(i -> {
			stoneTemplate.draw(g);
			g.translate(-inset, inset);
		});
		if (stonesLeft > 1) {
			g.setColor(player == app.getGame().getPlayerInTurn() ? Color.RED : Color.DARK_GRAY);
			g.setFont(stonesCounterFont);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(String.valueOf(stonesLeft), 2 * stoneTemplate.getRadius(), stoneTemplate.getRadius());
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
		g.translate(-x, -y);
	}
}