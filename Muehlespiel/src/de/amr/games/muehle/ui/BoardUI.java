package de.amr.games.muehle.ui;

import static de.amr.easy.game.math.Vector2f.dist;
import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.BoardGraph;
import de.amr.games.muehle.board.StoneColor;

/**
 * Board user interface.
 * 
 * @author Armin Reichert
 */
public class BoardUI extends GameEntity {

	private static final int[] GRID_X = { 0, 3, 6, 1, 3, 5, 2, 3, 4, 0, 1, 2, 4, 5, 6, 2, 3, 4, 1, 3, 5, 0, 3, 6 };
	private static final int[] GRID_Y = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6 };

	private final Board board;
	private final int width;
	private final int height;
	private final int[] xpos;
	private final int[] ypos;
	private final Stone[] stones;
	private Color bgColor;
	private Color lineColor;
	private Font font;
	private boolean positionNumbersOn;

	public BoardUI(Board board, int width, int height, Color bgColor, Color lineColor) {
		this.board = board;
		this.width = width;
		this.height = height;
		this.xpos = new int[BoardGraph.NUM_POS];
		this.ypos = new int[BoardGraph.NUM_POS];
		IntStream.range(0, BoardGraph.NUM_POS).forEach(p -> {
			xpos[p] = GRID_X[p] * width / 6;
			ypos[p] = GRID_Y[p] * height / 6;
		});
		this.bgColor = bgColor;
		this.lineColor = lineColor;
		this.font = new Font("Arial", Font.PLAIN, stoneRadius() * 9 / 10);
		this.stones = new Stone[BoardGraph.NUM_POS];
	}

	public Board board() {
		return board;
	}

	public Stream<Stone> stones() {
		return Stream.of(stones).filter(Objects::nonNull);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public int stoneRadius() {
		return width / 24;
	}

	private int posRadius() {
		return width / 60;
	}

	public void clear() {
		board.clear();
		Arrays.fill(stones, null);
	}

	public void putStoneAt(int p, StoneColor color) {
		board.putStoneAt(p, color);
		Stone stone = new Stone(color, stoneRadius());
		stone.tf.moveTo(centerPoint(p));
		stones[p] = stone;
	}

	public Optional<Stone> stoneAt(int p) {
		return stones[p] == null ? Optional.empty() : Optional.of(stones[p]);
	}

	public void moveStone(int from, int to) {
		board.moveStone(from, to);
		Stone stone = stones[from];
		stone.tf.moveTo(centerPoint(to));
		stones[to] = stone;
		stones[from] = null;
	}

	public void removeStoneAt(int p) {
		board.removeStoneAt(p);
		stones[p] = null;
	}

	public Vector2f centerPoint(int p) {
		return Vector2f.of(xpos[p], ypos[p]);
	}

	public OptionalInt findNearestPosition(int x, int y, int radius) {
		return board.positions().filter(p -> dist(centerPoint(p), Vector2f.of(x, y)) <= radius).findFirst();
	}

	public OptionalInt findPosition(int x, int y) {
		int boardX = abs(round(x - tf.getX()));
		int boardY = abs(round(y - tf.getY()));
		return findNearestPosition(boardX, boardY, stoneRadius());
	}

	public void showPositionNumbers() {
		positionNumbersOn = true;
	}

	public void togglePositionNumbers() {
		positionNumbersOn = !positionNumbersOn;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		// Background
		g.setColor(bgColor);
		g.fillRect(0, 0, width, height);
		// Lines
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(lineColor);
		g.setStroke(new BasicStroke(posRadius() / 2));
		board.positions().forEach(from -> {
			Vector2f fromPoint = centerPoint(from);
			board.neighbors(from).forEach(to -> {
				Vector2f toPoint = centerPoint(to);
				g.drawLine(fromPoint.roundedX(), fromPoint.roundedY(), toPoint.roundedX(), toPoint.roundedY());
			});
		});
		// Positions
		board.positions().forEach(p -> {
			Vector2f center = centerPoint(p);
			g.setColor(lineColor);
			g.fillOval(center.roundedX() - posRadius(), center.roundedY() - posRadius(), 2 * posRadius(), 2 * posRadius());
			if (positionNumbersOn) {
				g.setFont(font);
				g.drawString(String.valueOf(p), center.x + 2 * posRadius(), center.y + 4 * posRadius());
			}
		});
		// Stones
		stones().forEach(stone -> stone.draw(g));
		g.translate(-tf.getX(), -tf.getY());
	}

	public void markPosition(Graphics2D g, int p, Color color) {
		int markerSize = posRadius() * 8 / 10;
		Vector2f center = centerPoint(p);
		g.translate(tf.getX(), tf.getY());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.fillOval(round(center.x) - markerSize / 2, round(center.y) - markerSize / 2, markerSize, markerSize);
		g.translate(-tf.getX(), -tf.getY());
	}

	public void markPositions(Graphics2D g, IntStream positions, Color color) {
		positions.forEach(p -> markPosition(g, p, color));
	}

	public void markRemovableStones(Graphics2D g, StoneColor stoneColor) {
		boolean allStonesInMills = board.allStonesInMills(stoneColor);
		board.positions(stoneColor).filter(p -> allStonesInMills || !board.inMill(p, stoneColor)).forEach(p -> {
			stoneAt(p).ifPresent(stone -> {
				float offsetX = tf.getX() + stone.tf.getX() - stone.getWidth() / 2;
				float offsetY = tf.getY() + stone.tf.getY() - stone.getHeight() / 2;
				// draw red cross
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.translate(offsetX, offsetY);
				g.setColor(Color.RED);
				g.drawLine(0, 0, stone.getWidth(), stone.getHeight());
				g.drawLine(0, stone.getHeight(), stone.getWidth(), 0);
				g.translate(-offsetX, -offsetY);
			});
		});
	}
}