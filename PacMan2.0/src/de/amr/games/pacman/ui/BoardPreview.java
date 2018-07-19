package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Tile.DOOR;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;
import static de.amr.games.pacman.model.Tile.TUNNEL;
import static de.amr.games.pacman.model.Tile.WALL;
import static de.amr.games.pacman.model.Tile.WORMHOLE;

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
import de.amr.easy.grid.ui.swing.rendering.PearlsGridRenderer;
import de.amr.games.pacman.model.Maze;

public class BoardPreview extends JFrame {

	static int TS = 32;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(BoardPreview::new);
	}

	private Maze maze;

	public BoardPreview() {
		maze = Maze.of(Assets.text("maze.txt"));
		setTitle("Pac-Man Maze Preview");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		GridCanvas canvas = new GridCanvas(maze, TS);
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
		colors.put(DOOR, Color.ORANGE.darker());
		colors.put(WORMHOLE, Color.PINK);
		colors.put(TUNNEL, Color.PINK.darker());
		ConfigurableGridRenderer r = new PearlsGridRenderer();
		r.fnCellSize = () -> TS;
		r.fnPassageWidth = () -> 4;
		r.fnPassageColor = (cell, dir) -> Color.LIGHT_GRAY;
		r.fnCellBgColor = cell -> colors.getOrDefault(maze.get(cell), Color.WHITE);
		return r;
	}
}