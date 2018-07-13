package de.amr.games.pacman.board;

import static de.amr.games.pacman.board.Tile.DOOR;
import static de.amr.games.pacman.board.Tile.ENERGIZER;
import static de.amr.games.pacman.board.Tile.GHOSTHOUSE;
import static de.amr.games.pacman.board.Tile.PELLET;
import static de.amr.games.pacman.board.Tile.TUNNEL;
import static de.amr.games.pacman.board.Tile.WALL;
import static de.amr.games.pacman.board.Tile.WORMHOLE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.grid.ui.swing.rendering.ConfigurableGridRenderer;
import de.amr.easy.grid.ui.swing.rendering.GridCanvas;
import de.amr.easy.grid.ui.swing.rendering.GridRenderer;
import de.amr.easy.grid.ui.swing.rendering.WallPassageGridRenderer;

public class BoardPreview extends JFrame {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(BoardPreview::new);
	}

	private Board board;
	private int tileSize = 16;

	public BoardPreview() {
		board = new Board(Assets.text("maze.txt"));
		setTitle("PacMan Preview");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		GridCanvas canvas = new GridCanvas(board.grid, tileSize);
		canvas.pushRenderer(createRenderer());
		add(canvas, BorderLayout.CENTER);
		canvas.drawGrid();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private GridRenderer createRenderer() {
		Map<Character, Color> colors = new HashMap<>();
		colors.put(WALL, Color.BLUE);
		colors.put(PELLET, Color.YELLOW);
		colors.put(ENERGIZER, Color.GREEN);
		colors.put(GHOSTHOUSE, Color.ORANGE);
		colors.put(DOOR, Color.ORANGE.darker());
		colors.put(WORMHOLE, Color.PINK);
		colors.put(TUNNEL, Color.PINK.darker());
		ConfigurableGridRenderer r = new WallPassageGridRenderer();
		r.fnCellSize = () -> tileSize;
		r.fnPassageWidth = () -> tileSize - 1;
		r.fnPassageColor = (cell, dir) -> Color.WHITE;
		r.fnCellBgColor = cell -> colors.getOrDefault(board.grid.get(cell), Color.BLACK);
		return r;
	}
}