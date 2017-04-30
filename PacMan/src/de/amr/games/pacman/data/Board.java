package de.amr.games.pacman.data;

import java.util.Optional;
import java.util.stream.Stream;

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

	public static final Topology TOPOLOGY = new Top4();

	private final String[] boardDataRows;
	public final Grid<Character, Integer> grid;

	/**
	 * Initializes the board from the specified textual data.
	 * 
	 * @param boardAsText
	 *          board data as read from text file
	 */
	public Board(String boardAsText) {
		boardDataRows = boardAsText.split("\n");
		grid = new Grid<>(NUM_COLS, NUM_ROWS, TileContent.None.toChar(), false);
		reset();
	}

	/**
	 * Resets the board to its initial content.
	 */
	public void reset() {
		grid.vertexStream().forEach(cell -> grid.set(cell, boardDataRows[grid.row(cell)].charAt(grid.col(cell))));
	}

	/**
	 * Tells if the specified tile is valid for this board.
	 * 
	 * @param tile
	 *          a tile
	 * @return <code>true</code> if the tile is valid
	 */
	public boolean isTileValid(Tile tile) {
		int row = tile.getRow(), col = tile.getCol();
		return row >= 0 && row < NUM_ROWS && col >= 0 && col < NUM_COLS;
	}

	/**
	 * Sets the content of the specified tile.
	 * 
	 * @param tile
	 *          a tile
	 * @param content
	 *          some tile content
	 */
	public void setContent(Tile tile, TileContent content) {
		Integer cell = grid.cell(tile.getCol(), tile.getRow());
		grid.set(cell, content.toChar());
	}

	/**
	 * Tells if the given tile contains the given content.
	 * 
	 * @param tile
	 *          a tile
	 * @param content
	 *          some tile content
	 * @return <code>true</code> if the tile contains this content
	 */
	public boolean contains(Tile tile, TileContent content) {
		return contains(tile.getRow(), tile.getCol(), content);
	}

	/**
	 * Tells if the tile with the given coordinates contains the given content.
	 * 
	 * @param row
	 *          tile row
	 * @param col
	 *          tile column
	 * @param content
	 *          some content
	 * @return <code>true</code> if the tile contains this content
	 */
	public boolean contains(int row, int col, TileContent content) {
		return content.toChar() == grid.get(grid.cell(col, row));
	}

	/**
	 * Checks if the given tile contains the specified content.
	 * 
	 * @param tile
	 *          a tile
	 * @param content
	 *          some content
	 * @return an Optional containing the tile if it contains the content or an empty Optional if it
	 *         doesn't.
	 */
	public Optional<Tile> getContent(Tile tile, TileContent content) {
		return contains(tile.getRow(), tile.getCol(), content) ? Optional.of(tile) : Optional.empty();
	}

	public TileContent getContent(Tile tile) {
		return TileContent.valueOf(grid.get(grid.cell(tile.getCol(), tile.getRow())));
	}
	
	/**
	 * Returns the number of occurrences of the given content inside the whole board.
	 * 
	 * @param content
	 *          some tile content
	 * @return the number of occurrences of this content
	 */
	public long count(TileContent content) {
		return grid.vertexStream().filter(cell -> content.toChar() == grid.get(cell)).count();
	}

	/**
	 * Returns a stream of all tiles containg the given content.
	 * 
	 * @param content
	 *          some tile content
	 * @return a stream of all tiles with that content
	 */
	public Stream<Tile> tilesWithContent(TileContent content) {
		///*@formatter:off*/
		return grid.vertexStream()
				.filter(cell -> grid.get(cell) == content.toChar())
				.map(cell -> new Tile(grid.row(cell), grid.col(cell)));
		///*@formatter:on*/
	}
}