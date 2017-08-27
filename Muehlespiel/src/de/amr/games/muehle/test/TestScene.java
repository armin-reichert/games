package de.amr.games.muehle.test;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;
import static java.util.stream.Collectors.joining;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.IntStream;

import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.scene.Scene;
import de.amr.games.muehle.MillTestApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.ui.BoardUI;

public class TestScene extends Scene<MillTestApp> {

	static final Color BOARD_COLOR = Color.WHITE;
	static final Color LINE_COLOR = Color.BLACK;

	Board board;
	BoardUI boardUI;

	public TestScene(MillTestApp app) {
		super(app);
	}

	@Override
	public void init() {
		board = new Board();
		boardUI = new BoardUI(board, 600, 600, BOARD_COLOR, LINE_COLOR);
		boardUI.showPositionNumbers();

		boardUI.center(getWidth(), getHeight());
	}

	@Override
	public void update() {
		readInput();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(BOARD_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		boardUI.draw(g);
	}

	void readInput() {
		if (Mouse.clicked()) {
			handleMouseClick();
			updateStatus();
		}
	}

	void handleMouseClick() {
		boardUI.findPosition(Mouse.getX(), Mouse.getY()).ifPresent(pos -> {
			// LOG.info("Mouse clicked at board position " + pos);
			if (board.isEmptyPosition(pos)) {
				boardUI.putStoneAt(pos, Mouse.isLeftButton() ? WHITE : BLACK);
			} else {
				boardUI.removeStoneAt(pos);
			}
		});
	}

	void updateStatus() {
		LOG.info("Positions opening two white mills: " + toCSV(board.positionsOpeningTwoMills(WHITE)));
		LOG.info("Positions opening two black mills: " + toCSV(board.positionsOpeningTwoMills(BLACK)));
		LOG.info("Positions opening one white mill: " + toCSV(board.positionsOpeningMill(WHITE)));
		LOG.info("Positions opening one black mill: " + toCSV(board.positionsOpeningMill(BLACK)));
		LOG.info("Positions closing white mill: " + toCSV(board.positionsClosingMill(WHITE)));
		LOG.info("Positions closing black mill: " + toCSV(board.positionsClosingMill(BLACK)));
		LOG.info("Positions from where can close white mill: "
				+ toCSV(board.positions().filter(p -> board.canCloseMillMovingFrom(p, WHITE))));
		LOG.info("Positions from where can close black mill: "
				+ toCSV(board.positions().filter(p -> board.canCloseMillMovingFrom(p, BLACK))));
	}

	String toCSV(IntStream stream) {
		return stream.mapToObj(String::valueOf).collect(joining(", "));
	}

}
