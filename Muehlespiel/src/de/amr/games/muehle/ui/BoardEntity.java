package de.amr.games.muehle.ui;

import static de.amr.easy.game.math.Vector2.dist;
import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public class BoardEntity extends GameEntity {

	/*
	 * (GRID_X[p], GRID_Y[p]) is the grid coordinate of position p in the board's [0..6] x [0..6] grid.
	 */
	private static final int[] GRID_X = { 0, 3, 6, 1, 3, 5, 2, 3, 4, 0, 1, 2, 4, 5, 6, 2, 3, 4, 1, 3, 5, 0, 3, 6 };
	private static final int[] GRID_Y = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6 };

	private final Board board;
	private StoneEntity[] stones;
	private int width;
	private int height;
	private int posRadius;
	private boolean showPositionNumbers;

	public BoardEntity(Board board, int w, int h) {
		this.board = board;
		width = w;
		height = h;
		posRadius = w / 60;
		stones = new StoneEntity[Board.NUM_POS];
		StoneEntity.radius = width / 24;
	}

	public Board getBoard() {
		return board;
	}

	public Stream<StoneEntity> stones() {
		return Stream.of(stones).filter(Objects::nonNull);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			showPositionNumbers = !showPositionNumbers;
		}
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public void putStoneAt(int p, StoneColor color) {
		board.putStoneAt(p, color);
		StoneEntity stone = new StoneEntity(color);
		stone.tf.moveTo(centerPoint(p));
		stones[p] = stone;
	}

	public StoneEntity getStoneAt(int p) {
		return stones[p];
	}

	public void moveStone(int from, int to) {
		board.moveStone(from, to);
		StoneEntity stone = stones[from];
		stone.tf.moveTo(centerPoint(to));
		stones[to] = stone;
		stones[from] = null;
	}

	public void removeStoneAt(int p) {
		board.removeStoneAt(p);
		stones[p] = null;
	}

	public Vector2 centerPoint(int p) {
		return new Vector2(GRID_X[p] * width / 6, GRID_Y[p] * height / 6);
	}

	public int findNearestPosition(int x, int y, int radius) {
		return board.positions().filter(p -> dist(centerPoint(p), new Vector2(x, y)) <= radius).findFirst().orElse(-1);
	}

	public int findPosition(int x, int y) {
		int boardX = abs(round(x - tf.getX()));
		int boardY = abs(round(y - tf.getY()));
		return findNearestPosition(boardX, boardY, getWidth() / 18);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Hintergrund
		g.setColor(new Color(255, 255, 224));
		g.fillRect(0, 0, width, height);

		// Linien
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(posRadius / 2));
		board.positions().forEach(p -> {
			Vector2 centerFrom = centerPoint(p);
			board.neighbors(p).forEach(q -> {
				Vector2 centerTo = centerPoint(q);
				g.drawLine(centerFrom.roundedX(), centerFrom.roundedY(), centerTo.roundedX(), centerTo.roundedY());
			});
		});

		// Positionen
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.PLAIN, 20));
		board.positions().forEach(p -> {
			Vector2 center = centerPoint(p);
			g.fillOval(center.roundedX() - posRadius, center.roundedY() - posRadius, 2 * posRadius, 2 * posRadius);
			if (showPositionNumbers) {
				g.drawString(p + "", center.x + 3 * posRadius, center.y + 3 * posRadius);
			}
		});

		// Steine
		stones().forEach(stone -> stone.draw(g));

		g.translate(-tf.getX(), -tf.getY());
	}

}
