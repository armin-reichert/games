package de.amr.games.pacman.data;

import java.util.Optional;

import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Grid;
import de.amr.easy.grid.impl.Top4;

/**
 * The board for the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Board {

	public static final int NUM_ROWS = 36;
	public static final int NUM_COLS = 28;

	public static final float PACMAN_HOME_ROW = 26;
	public static final float PACMAN_HOME_COL = 13.5f;

	public static final float BLINKY_HOME_ROW = 14;
	public static final float BLINKY_HOME_COL = 13.5f;

	public static final float INKY_HOME_ROW = 17.5f;
	public static final float INKY_HOME_COL = 11.5f;

	public static final float PINKY_HOME_ROW = 17.5f;
	public static final float PINKY_HOME_COL = 13.5f;

	public static final float CLYDE_HOME_ROW = 17.5f;
	public static final float CLYDE_HOME_COL = 15.5f;

	public static final float BONUS_ROW = 19.5f;
	public static final float BONUS_COL = 13;

	public final Topology topology = new Top4();
	public final Grid<Character, Integer> grid;

	private final String[] boardDataRows;

	public Board(String boardData) {
		boardDataRows = boardData.split("\n");
		grid = new Grid<>(NUM_COLS, NUM_ROWS, TileContent.Empty.toChar(), false);
		reset();
	}

	public void reset() {
		grid.vertexStream().forEach(cell -> grid.set(cell, boardDataRows[grid.row(cell)].charAt(grid.col(cell))));
	}

	public boolean isTileValid(Tile tile) {
		int row = tile.getRow(), col = tile.getCol();
		return row >= 0 && row < NUM_ROWS && col >= 0 && col < NUM_COLS;
	}

	public void setContent(Tile tile, TileContent content) {
		Integer cell = grid.cell(tile.getCol(), tile.getRow());
		grid.set(cell, content.toChar());
	}

	public boolean has(TileContent content, Tile tile) {
		return has(content, tile.getRow(), tile.getCol());
	}

	public boolean has(TileContent content, int row, int col) {
		Integer cell = grid.cell(col, row);
		return content.toChar() == grid.get(cell);
	}

	public Optional<Tile> checkContent(Tile tile, TileContent content) {
		return has(content, tile.getRow(), tile.getCol()) ? Optional.of(tile) : Optional.empty();
	}

	public long count(TileContent content) {
		return grid.vertexStream().filter(cell -> content.toChar() == grid.get(cell)).count();
	}
}