package de.amr.games.muehle.game.api;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Optional;
import java.util.stream.IntStream;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Stone;

public interface MillGameUI {

	static final Color BOARD_COLOR = new Color(255, 255, 224);
	static final Color LINE_COLOR = Color.BLACK;

	void clearBoard();

	Optional<Stone> getStoneAt(int p);

	void removeStoneAt(int p);

	void putStoneAt(int p, StoneColor color);

	void moveStone(int from, int to);

	void showMessage(String key, Object... args);

	Vector2f centerPoint(int p);

	void markPosition(Graphics2D g, int p, Color color);

	void markPositions(Graphics2D g, IntStream positions, Color color);

}