package de.amr.games.pacman.scenes;

import static de.amr.games.pacman.data.Board.NUM_COLS;
import static de.amr.games.pacman.data.Board.NUM_ROWS;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.List;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.data.TileContent;
import de.amr.games.pacman.ui.PacManUI;

public class DrawUtil {

	private static Image gridLines;

	private static Image getGridImage(int width, int height) {
		if (gridLines == null) {
			gridLines = PacManUI.createTransparentImage(width, height);
			Graphics g = gridLines.getGraphics();
			g.setColor(new Color(200, 200, 200, 100));
			for (int col = 1, x = TILE_SIZE; col < NUM_COLS; ++col, x += TILE_SIZE) {
				g.drawLine(x, 0, x, height);
			}
			for (int row = 1, y = TILE_SIZE; row < NUM_ROWS; ++row, y += TILE_SIZE) {
				g.drawLine(0, y, width, y);
			}
		}
		return gridLines;
	}

	public static void drawGridLines(Graphics2D g, int width, int height) {
		g.drawImage(getGridImage(width, height), 0, 0, null);
	}

	public static void drawSprite(Graphics2D g, float row, float col, Sprite sprite) {
		float x = TILE_SIZE * col, y = TILE_SIZE * row;
		g.translate(x, y);
		sprite.draw(g);
		g.translate(-x, -y);
	}

	public static void drawText(Graphics2D g, float row, float col, String text) {
		g.drawString(text, TILE_SIZE * col, TILE_SIZE * row);
	}

	public static void drawTextCentered(Graphics2D g, int width, float row, String text) {
		g.drawString(text, (width - g.getFontMetrics().stringWidth(text)) / 2, TILE_SIZE * row);
	}

	public static void drawRouteMap(Graphics2D g, Board board) {
		g.setColor(Color.WHITE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int radius = TILE_SIZE / 2;
		board.graph.vertexStream()
				.filter(cell -> !board.contains(board.graph.row(cell), board.graph.col(cell), TileContent.Wall))
				.forEach(cell -> {
					g.fillOval(board.graph.col(cell) * TILE_SIZE + radius / 2, board.graph.row(cell) * TILE_SIZE + radius / 2,
							radius, radius);
				});
		board.graph.edgeStream().forEach(edge -> {
			Integer from = edge.either(), to = edge.other(from);
			drawEdge(g, board, from, to);
		});
	}

	public static void drawRoute(Graphics2D g, Board board, Tile start, List<Integer> route) {
		Tile from = start, to = null;
		for (Integer dir : route) {
			int dx = board.topology.dx(dir), dy = board.topology.dy(dir);
			to = new Tile(from);
			to.translate(dx, dy);
			int offset = TILE_SIZE / 4;
			int x1 = from.getCol() * TILE_SIZE + offset;
			int y1 = from.getRow() * TILE_SIZE + offset;
			int x2 = to.getCol() * TILE_SIZE + offset;
			int y2 = to.getRow() * TILE_SIZE + offset;
			g.fillOval(x1, y1, TILE_SIZE / 2, TILE_SIZE / 2);
			g.drawLine(x1 + offset, y1 + offset, x2 + offset, y2 + offset);
			g.fillOval(x2, y2, TILE_SIZE / 2, TILE_SIZE / 2);
			from = to;
		}
	}

	private static void drawEdge(Graphics2D g, Board board, Integer from, Integer to) {
		int offset = TILE_SIZE / 2;
		int x1 = board.graph.col(from) * TILE_SIZE + offset;
		int y1 = board.graph.row(from) * TILE_SIZE + offset;
		int x2 = board.graph.col(to) * TILE_SIZE + offset;
		int y2 = board.graph.row(to) * TILE_SIZE + offset;
		g.drawLine(x1, y1, x2, y2);
	}

}