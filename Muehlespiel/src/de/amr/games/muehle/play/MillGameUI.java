package de.amr.games.muehle.play;

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

	public void clearBoard();

	public Optional<Stone> getStoneAt(int p);

	public void removeStoneAt(int p);

	public void putStoneAt(int p, StoneColor color);

	public void moveStone(int from, int to);

	public void showMessage(String key, Object... args);

	public Vector2f centerPoint(int p);

	public void markPosition(Graphics2D g, int p, Color color);

	public void markPositions(Graphics2D g, IntStream positions, Color color);

}
