package de.amr.games.pacman.core.board;

import static de.amr.easy.grid.impl.Top4.Top4;
import static de.amr.games.pacman.core.board.TileContent.None;
import static de.amr.games.pacman.core.board.TileContent.Outside;
import static de.amr.games.pacman.core.board.TileContent.Wall;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.graph.alg.traversal.BreadthFirstTraversal;
import de.amr.easy.graph.api.PathFinder;
import de.amr.easy.grid.impl.Grid;

/**
 * The board of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Board {

	public final int numRows;
	public final int numCols;
	public final Grid<Character, Integer> graph;
	private final String[] boardRows;

	/**
	 * Initializes the board from the specified textual data.
	 * 
	 * @param boardRows
	 *          board data rows as text
	 */
	public Board(String boardDefinition) {
		this.boardRows = boardDefinition.split("\n");
		numRows = boardRows.length;
		numCols = boardRows[0].length();
		// create orthogonal grid graph from board data
		graph = new Grid<>(numCols, numRows, None.toChar(), false);
		graph.setTopology(Top4);
		resetContent();
		/*@formatter:off*/
		graph.vertexStream()
			.filter(tile -> graph.get(tile) != Wall.toChar())
			.forEach(tile -> {
				Top4.dirs().forEach(dir -> {
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
		int row = tile.row, col = tile.col;
		return row >= 0 && row < numRows && col >= 0 && col < numCols && getOriginalContent(row, col) != Outside.toChar();
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
		Integer cell = graph.cell(tile.col, tile.row);
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
		return contains(tile.row, tile.col, content);
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
		return contains(tile.row, tile.col, content) ? Optional.of(tile) : Optional.empty();
	}

	/**
	 * Returns the content at the specified tile.
	 * 
	 * @param tile
	 *          a tile
	 * @return the tile content
	 */
	public TileContent getContent(Tile tile) {
		return getContent(tile.row, tile.col);
	}

	/**
	 * Returns the content at the specified tile position.
	 * 
	 * @param row
	 *          a row
	 * @param col
	 *          a col
	 * @return the tile content
	 */
	public TileContent getContent(int row, int col) {
		return TileContent.valueOf(graph.get(graph.cell(col, row)));
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
		Integer sourceCell = graph.cell(source.col, source.row);
		Integer targetCell = graph.cell(target.col, target.row);
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