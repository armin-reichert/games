package de.amr.games.muehle.test;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.model.board.Board.positions;
import static de.amr.games.muehle.model.board.StoneColor.BLACK;
import static de.amr.games.muehle.model.board.StoneColor.WHITE;
import static java.util.stream.Collectors.joining;

import java.awt.Graphics2D;
import java.util.stream.IntStream;

import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.scene.Scene;
import de.amr.games.muehle.model.board.Board;
import de.amr.games.muehle.view.BoardUI;
import de.amr.games.muehle.view.MillGameUI;

public class TestScene extends Scene<MillTestApp> {

	private final Board board;
	private BoardUI boardUI;

	public TestScene(MillTestApp app) {
		super(app);
		setBgColor(MillGameUI.BOARD_COLOR);
		board = new Board();
	}

	@Override
	public void init() {
		boardUI = new BoardUI(board);
		boardUI.setSize(600);
		boardUI.setBgColor(MillGameUI.BOARD_COLOR);
		boardUI.setLineColor(MillGameUI.LINE_COLOR);
		boardUI.showPositionNumbers();
		boardUI.center(getWidth(), getHeight());
	}

	@Override
	public void update() {
		if (Mouse.clicked()) {
			handleMouseClick();
			printBoardInfo();
		}
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		boardUI.draw(g);
	}

	private void handleMouseClick() {
		boardUI.findBoardPosition(Mouse.getX(), Mouse.getY()).ifPresent(pos -> {
			if (board.isEmptyPosition(pos)) {
				boardUI.putStoneAt(pos, Mouse.isLeftButton() ? WHITE : BLACK);
			} else {
				boardUI.removeStoneAt(pos);
			}
		});
	}

	private void printBoardInfo() {
		LOG.info("Positions opening two white mills: " + toCSV(board.positionsOpeningTwoMills(WHITE)));
		LOG.info("Positions opening two black mills: " + toCSV(board.positionsOpeningTwoMills(BLACK)));
		LOG.info("Positions opening one white mill: " + toCSV(board.positionsOpeningMill(WHITE)));
		LOG.info("Positions opening one black mill: " + toCSV(board.positionsOpeningMill(BLACK)));
		LOG.info("Positions closing white mill: " + toCSV(board.positionsClosingMill(WHITE)));
		LOG.info("Positions closing black mill: " + toCSV(board.positionsClosingMill(BLACK)));
		LOG.info("Positions from where can close white mill: "
				+ toCSV(positions().filter(p -> board.canCloseMillMovingFrom(p, WHITE))));
		LOG.info("Positions from where can close black mill: "
				+ toCSV(positions().filter(p -> board.canCloseMillMovingFrom(p, BLACK))));
	}

	private String toCSV(IntStream stream) {
		return stream.mapToObj(String::valueOf).collect(joining(", "));
	}

}