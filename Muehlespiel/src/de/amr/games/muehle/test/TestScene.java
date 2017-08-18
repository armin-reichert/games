package de.amr.games.muehle.test;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;
import static java.util.stream.Collectors.joining;

import java.awt.Graphics2D;

import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.scene.Scene;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.ui.BoardUI;

public class TestScene extends Scene<MillApp> {

	Board board;
	BoardUI boardUI;

	public TestScene(MillApp app) {
		super(app);
	}

	@Override
	public void init() {
		board = new Board();
		boardUI = new BoardUI(board, 600, 600);
		boardUI.showPositionNumbers();

		boardUI.center(getWidth(), getHeight());
	}

	@Override
	public void update() {
		readInput();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(BoardUI.BOARD_COLOR);
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
		LOG.info("Positions opening two white mills: "
				+ board.positionsOpeningTwoMills(WHITE).mapToObj(String::valueOf).collect(joining(", ")));
		LOG.info("Positions opening two black mills: "
				+ board.positionsOpeningTwoMills(BLACK).mapToObj(String::valueOf).collect(joining(", ")));
		LOG.info("Positions opening one white mill: "
				+ board.positionsOpeningMill(WHITE).mapToObj(String::valueOf).collect(joining(", ")));
		LOG.info("Positions opening one black mill: "
				+ board.positionsOpeningMill(BLACK).mapToObj(String::valueOf).collect(joining(", ")));
	}

}
