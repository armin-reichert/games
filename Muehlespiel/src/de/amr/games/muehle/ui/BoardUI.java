package de.amr.games.muehle.ui;

import static de.amr.easy.game.math.Vector2f.dist;
import static de.amr.games.muehle.board.BoardGraph.NUM_POS;
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
	private final Stone[] stones;
	private final int[] xpos;
	private final int[] ypos;
	private int size;
	private int rasterSize;
	private Color bgColor;
	private Color lineColor;
	private Font font;
	private boolean positionNumbersOn;

	public BoardUI(Board board) {
		this.board = board;
		this.stones = new Stone[NUM_POS];
		this.xpos = new int[NUM_POS];
		this.ypos = new int[NUM_POS];
	}

	public void setSize(int size) {
		this.size = size;
		this.rasterSize = size / 6;
		IntStream.range(0, NUM_POS).forEach(p -> {
			xpos[p] = GRID_X[p] * rasterSize;
			ypos[p] = GRID_Y[p] * rasterSize;
		});
		this.font = new Font("Arial", Font.PLAIN, rasterSize * 9 / 40);
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	@Override
	public int getWidth() {
		return size;
	}

	@Override
	public int getHeight() {
		return size;
	}

	public int getStoneRadius() {
		return rasterSize / 4;
	}

	public void clear() {
		board.clear();
		Arrays.fill(stones, null);
	}

	public void putStoneAt(int p, StoneColor color) {
		board.putStoneAt(p, color);
		Stone stone = new Stone(color, getStoneRadius());
		stone.tf.moveTo(centerPoint(p));
		stones[p] = stone;
	}

	public Optional<Stone> stoneAt(int p) {
		return Optional.ofNullable(stones[p]);
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
		Vector2f point = Vector2f.of(x, y);
		return board.positions().filter(p -> dist(centerPoint(p), point) <= radius).findFirst();
	}

	public OptionalInt findPosition(int x, int y) {
		int boardX = abs(round(x - tf.getX()));
		int boardY = abs(round(y - tf.getY()));
		return findNearestPosition(boardX, boardY, getStoneRadius());
	}

	public void showPositionNumbers() {
		positionNumbersOn = true;
	}

	public void togglePositionNumbers() {
		positionNumbersOn = !positionNumbersOn;
	}

	private void aa_on(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	private void aa_off(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	@Override
	public void draw(Graphics2D g) {
		final int posRadius = rasterSize / 10;
		g.translate(tf.getX(), tf.getY());
		// Background
		g.setColor(bgColor);
		g.fillRect(0, 0, size, size);
		// Lines
		g.setColor(lineColor);
		g.setStroke(new BasicStroke(posRadius / 2));
		board.positions().forEach(from -> {
			Vector2f fromPoint = centerPoint(from);
			board.neighbors(from).filter(to -> to > from).forEach(to -> {
				Vector2f toPoint = centerPoint(to);
				g.drawLine(fromPoint.roundedX(), fromPoint.roundedY(), toPoint.roundedX(), toPoint.roundedY());
			});
		});
		// Positions
		board.positions().forEach(p -> {
			Vector2f center = centerPoint(p);
			g.setColor(lineColor);
			aa_on(g);
			g.fillOval(center.roundedX() - posRadius, center.roundedY() - posRadius, 2 * posRadius, 2 * posRadius);
			aa_off(g);
			if (positionNumbersOn) {
				g.setFont(font);
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.drawString(String.valueOf(p), center.x + 2 * posRadius, center.y + 4 * posRadius);
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}
		});
		// Stones
		Stream.of(stones).filter(Objects::nonNull).forEach(stone -> stone.draw(g));
		g.translate(-tf.getX(), -tf.getY());
	}

	public void markPosition(Graphics2D g, int p, Color color) {
		int markerSize = rasterSize * 8 / 100;
		Vector2f center = centerPoint(p);
		g.translate(tf.getX(), tf.getY());
		g.setColor(color);
		aa_on(g);
		g.fillOval(round(center.x) - markerSize / 2, round(center.y) - markerSize / 2, markerSize, markerSize);
		aa_off(g);
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
				g.translate(offsetX, offsetY);
				g.setColor(Color.RED);
				aa_on(g);
				g.drawLine(0, 0, stone.getWidth(), stone.getHeight());
				g.drawLine(0, stone.getHeight(), stone.getWidth(), 0);
				aa_off(g);
				g.translate(-offsetX, -offsetY);
			});
		});
	}
}