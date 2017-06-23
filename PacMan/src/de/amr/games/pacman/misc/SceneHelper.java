package de.amr.games.pacman.misc;

import static de.amr.easy.grid.impl.Top4.Top4;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Grid;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;

public class SceneHelper {

	private static Image gridLines;

	private static Image getGridImage(int width, int height) {
		if (gridLines == null) {
			gridLines = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics g = gridLines.getGraphics();
			g.setColor(new Color(200, 200, 200, 100));
			int numCols = width / TILE_SIZE, numRows = height / TILE_SIZE;
			for (int col = 1, x = TILE_SIZE; col < numCols; ++col, x += TILE_SIZE) {
				g.drawLine(x, 0, x, height);
			}
			for (int row = 1, y = TILE_SIZE; row < numRows; ++row, y += TILE_SIZE) {
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
		Grid<?, ?> grid = board.getGraph();
		grid.vertexStream().filter(cell -> !board.contains(grid.row(cell), grid.col(cell), TileContent.Wall))
				.forEach(cell -> {
					g.fillOval(grid.col(cell) * TILE_SIZE + radius / 2, grid.row(cell) * TILE_SIZE + radius / 2, radius,
							radius);
				});
		grid.edgeStream().forEach(edge -> {
			Integer from = edge.either(), to = edge.other(from);
			drawEdge(g, board, from, to);
		});
	}

	public static void drawRoute(Graphics2D g, Board board, Tile start, List<Integer> route) {
		Tile from = start, to = null;
		for (Integer dir : route) {
			int dx = Top4.dx(dir), dy = Top4.dy(dir);
			to = new Tile(from).translate(dy, dx);
			int offset = TILE_SIZE / 4;
			int x1 = from.col * TILE_SIZE + offset;
			int y1 = from.row * TILE_SIZE + offset;
			int x2 = to.col * TILE_SIZE + offset;
			int y2 = to.row * TILE_SIZE + offset;
			// g.fillOval(x1, y1, TILE_SIZE / 2, TILE_SIZE / 2);
			g.drawLine(x1 + offset, y1 + offset, x2 + offset, y2 + offset);
			// g.fillOval(x2, y2, TILE_SIZE / 2, TILE_SIZE / 2);
			from = to;
		}
	}

	private static void drawEdge(Graphics2D g, Board board, Integer from, Integer to) {
		Grid<?, ?> grid = board.getGraph();
		int offset = TILE_SIZE / 2;
		int x1 = grid.col(from) * TILE_SIZE + offset;
		int y1 = grid.row(from) * TILE_SIZE + offset;
		int x2 = grid.col(to) * TILE_SIZE + offset;
		int y2 = grid.row(to) * TILE_SIZE + offset;
		g.drawLine(x1, y1, x2, y2);
	}

}