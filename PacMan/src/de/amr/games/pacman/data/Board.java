package de.amr.games.pacman.data;

import static de.amr.games.pacman.data.TileContent.None;
import static de.amr.games.pacman.data.TileContent.Wall;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.graph.alg.traversal.BreadthFirstTraversal;
import de.amr.easy.graph.api.PathFinder;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Grid;
import de.amr.easy.grid.impl.Top4;

/**
 * The board of the Pac-Man game.
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
	
	public static final Tile GHOST_HOUSE_ENTRY = new Tile(14, 13); 

	public final Topology topology;

	private final String[] boardRows;

	public final Grid<Character, Integer> graph;

	/**
	 * Initializes the board from the specified textual data.
	 * 
	 * @param boardRows
	 *          board data rows as text
	 */
	public Board(String[] boardRows) {
		this.boardRows = boardRows;
		// create orthogonal grid graph from board data
		topology = new Top4();
		graph = new Grid<>(NUM_COLS, NUM_ROWS, None.toChar(), false);
		graph.setTopology(topology);
		resetContent();
		/*@formatter:off*/
		graph.vertexStream()
			.filter(tile -> graph.get(tile) != Wall.toChar())
			.forEach(tile -> {
				topology.dirs().forEach(dir -> {
					graph.neighbor(tile, dir).ifPresent(neighbor -> {
						if (graph.get(neighbor) != Wall.toChar()	&& !graph.adjacent(tile, neighbor)) {
							graph.addEdge(tile, neighbor);
						}
					});
				});
			});
		/*@formatter:on*/
	}

	private char getOriginalContent(int row, int col) {
		return boardRows[row].charAt(col);
	}

	/**
	 * Resets the board to its initial content.
	 */
	public void resetContent() {
		graph.vertexStream().forEach(cell -> graph.set(cell, getOriginalContent(graph.row(cell), graph.col(cell))));
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
		Integer cell = graph.cell(tile.getCol(), tile.getRow());
		graph.set(cell, content.toChar());
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
		return content.toChar() == graph.get(graph.cell(col, row));
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

	/**
	 * Returns the content at the specified tile.
	 * 
	 * @param tile
	 *          a tile
	 * @return the tile content
	 */
	public TileContent getContent(Tile tile) {
		return TileContent.valueOf(graph.get(graph.cell(tile.getCol(), tile.getRow())));
	}

	/**
	 * Returns the number of occurrences of the given content inside the whole board.
	 * 
	 * @param content
	 *          some tile content
	 * @return the number of occurrences of this content
	 */
	public long count(TileContent content) {
		return graph.vertexStream().filter(cell -> content.toChar() == graph.get(cell)).count();
	}

	/**
	 * Returns a stream of all tiles containing the given content.
	 * 
	 * @param content
	 *          some tile content
	 * @return a stream of all tiles with that content
	 */
	public Stream<Tile> tilesWithContent(TileContent content) {
		///*@formatter:off*/
		return graph.vertexStream()
				.filter(cell -> graph.get(cell) == content.toChar())
				.map(cell -> new Tile(graph.row(cell), graph.col(cell)));
		///*@formatter:on*/
	}

	/**
	 * Computes the shortest route between the given tiles.
	 * 
	 * @param source
	 *          route start
	 * @param target
	 *          route target
	 * @return list of directions to walk from the source tile to reach the target tile
	 */
	public List<Integer> shortestRoute(Tile source, Tile target) {
		Integer sourceCell = graph.cell(source.getCol(), source.getRow());
		Integer targetCell = graph.cell(target.getCol(), target.getRow());
		PathFinder<Integer> pathFinder = new BreadthFirstTraversal<>(graph, sourceCell);
		pathFinder.run();
		List<Integer> route = new ArrayList<>();
		Integer pred = null;
		for (Integer cell : pathFinder.findPath(targetCell)) {
			if (pred != null) {
				route.add(graph.direction(pred, cell).getAsInt());
			}
			pred = cell;
		}
		return route;
	}
}