package de.amr.games.muehle.ui;

import static de.amr.easy.game.math.Vector2.dist;
import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2;
import de.amr.games.muehle.board.BoardModel;
import de.amr.games.muehle.board.StoneType;

/**
 * Board user interface.
 * 
 * @author Armin Reichert
 */
public class Board extends GameEntity {

	/*
	 * (GRID_X[p], GRID_Y[p]) is the grid coordinate of position p in the board's [0..6] x [0..6] grid.
	 */
	private static final int[] GRID_X = { 0, 3, 6, 1, 3, 5, 2, 3, 4, 0, 1, 2, 4, 5, 6, 2, 3, 4, 1, 3, 5, 0, 3, 6 };
	private static final int[] GRID_Y = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6 };

	private final BoardModel model;
	private final int width;
	private final int height;
	private final int posRadius;

	private boolean showPositionNumbers;
	private Stone[] stones;

	public Board(BoardModel model, int w, int h) {
		this.model = model;
		width = w;
		height = h;
		posRadius = w / 60;
		stones = new Stone[BoardModel.NUM_POS];
		Stone.radius = width / 24;
	}

	public BoardModel getModel() {
		return model;
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

	public void clear() {
		model.clear();
		stones = new Stone[BoardModel.NUM_POS];
	}

	public void putStoneAt(int p, StoneType color) {
		model.putStoneAt(p, color);
		Stone stone = new Stone(color);
		stone.tf.moveTo(centerPoint(p));
		stones[p] = stone;
	}

	public Optional<Stone> getStoneAt(int p) {
		return stones[p] == null ? Optional.empty() : Optional.of(stones[p]);
	}

	public void moveStone(int from, int to) {
		model.moveStone(from, to);
		Stone stone = stones[from];
		stone.tf.moveTo(centerPoint(to));
		stones[to] = stone;
		stones[from] = null;
	}

	public void removeStoneAt(int p) {
		model.removeStoneAt(p);
		stones[p] = null;
	}

	public Vector2 centerPoint(int p) {
		return new Vector2(GRID_X[p] * width / 6, GRID_Y[p] * height / 6);
	}

	public OptionalInt findNearestPosition(int x, int y, int radius) {
		return model.positions().filter(p -> dist(centerPoint(p), new Vector2(x, y)) <= radius).findFirst();
	}

	public OptionalInt findPosition(int x, int y) {
		int boardX = abs(round(x - tf.getX()));
		int boardY = abs(round(y - tf.getY()));
		return findNearestPosition(boardX, boardY, getWidth() / 18);
	}

	public void showPositionNumbers() {
		showPositionNumbers = true;
	}

	public void togglePositionNumbers() {
		showPositionNumbers = !showPositionNumbers;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Background
		g.setColor(new Color(255, 255, 224));
		g.fillRect(0, 0, width, height);

		// Lines
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(posRadius / 2));
		model.positions().forEach(p -> {
			Vector2 centerFrom = centerPoint(p);
			model.neighbors(p).forEach(q -> {
				Vector2 centerTo = centerPoint(q);
				g.drawLine(centerFrom.roundedX(), centerFrom.roundedY(), centerTo.roundedX(), centerTo.roundedY());
			});
		});

		// Positions
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.PLAIN, 20));
		model.positions().forEach(p -> {
			Vector2 center = centerPoint(p);
			g.fillOval(center.roundedX() - posRadius, center.roundedY() - posRadius, 2 * posRadius, 2 * posRadius);
			if (showPositionNumbers) {
				g.drawString(p + "", center.x + 3 * posRadius, center.y + 3 * posRadius);
			}
		});

		// Stones
		stones().forEach(stone -> stone.draw(g));

		g.translate(-tf.getX(), -tf.getY());
	}

	public void markPosition(Graphics2D g, int p, Color color) {
		int markerSize = posRadius * 8 / 10;
		Vector2 center = centerPoint(p);
		g.translate(tf.getX(), tf.getY());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.fillOval(round(center.x) - markerSize / 2, round(center.y) - markerSize / 2, markerSize, markerSize);
		g.translate(-tf.getX(), -tf.getY());
	}

	public void markPositionsOpeningTwoMills(Graphics2D g, StoneType stoneType, Color color) {
		model.positionsOpeningTwoMills(stoneType).forEach(p -> markPosition(g, p, color));
	}

	public void markPositionsClosingMill(Graphics2D g, StoneType stoneType, Color color) {
		model.positionsForClosingMill(stoneType).forEach(p -> markPosition(g, p, color));
	}

	public void markPositionFixingOpponent(Graphics2D g, StoneType either, StoneType other, Color color) {
		if (model.positionsWithEmptyNeighbor(other).count() == 1) {
			int singleFreePosition = model.positionsWithEmptyNeighbor(other).findFirst().getAsInt();
			if (model.neighbors(singleFreePosition).anyMatch(p -> model.getStoneAt(p) == either)) {
				markPosition(g, singleFreePosition, color);
			}
		}
	}

	public void markPossibleMoveStarts(Graphics2D g, StoneType type, boolean canJump) {
		IntStream startPositions = canJump ? model.positions(type) : model.positionsWithEmptyNeighbor(type);
		startPositions.forEach(p -> markPosition(g, p, Color.GREEN));
		startPositions.close();
	}

	public void markRemovableStones(Graphics2D g, StoneType stoneType) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		boolean allInMill = model.areAllStonesInsideMill(stoneType);
		model.positions(stoneType).filter(p -> allInMill || !model.isPositionInsideMill(p, stoneType))
				.forEach(p -> {
					Stone stone = getStoneAt(p).get();
					float offsetX = tf.getX() + stone.tf.getX() - stone.getWidth() / 2;
					float offsetY = tf.getY() + stone.tf.getY() - stone.getHeight() / 2;
					g.translate(offsetX, offsetY);
					g.setColor(Color.RED);
					g.drawLine(0, 0, stone.getWidth(), stone.getHeight());
					g.drawLine(0, stone.getHeight(), stone.getWidth(), 0);
					g.translate(-offsetX, -offsetY);
				});
	}
}