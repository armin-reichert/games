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
	private final Grid<Character, Integer> graph;
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
		loadContent();
		/*@formatter:off*/
		graph.vertexStream()
			.filter(cell -> graph.get(cell) != Wall.toChar())
			.forEach(cell -> {
				Top4.dirs().forEach(dir -> {
					graph.neighbor(cell, dir).ifPresent(neighbor -> {
						if (graph.get(neighbor) != Wall.toChar() && !graph.adjacent(cell, neighbor)) {
							graph.addEdge(cell, neighbor);
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
	 * @return the grid graph of this board
	 */
	public Grid<Character, Integer> getGraph() {
		return graph;
	}

	/**
	 * Loads the original board content.
	 */
	public void loadContent() {
		graph.vertexStream().forEach(cell -> graph.set(cell, getOriginalContent(graph.row(cell), graph.col(cell))));
	}

	/**
	 * Tells if the specified tile is part of this board.
	 * 
	 * @param tile
	 *          a tile
	 * @return <code>true</code> if the tile is part of this board
	 */
	public boolean isBoardTile(Tile tile) {
		return 0 <= tile.row && tile.row < numRows && 0 <= tile.col && tile.col < numCols
				&& getOriginalContent(tile.row, tile.col) != Outside.toChar();
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
		graph.set(graph.cell(tile.col, tile.row), content.toChar());
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
		/*@formatter:off*/
		return graph.vertexStream()
			.filter(cell -> graph.get(cell) == content.toChar())
			.map(cell -> new Tile(graph.row(cell), graph.col(cell)));
		/*@formatter:on*/
	}

	/**
	 * Computes the shortest route between the given tiles.
	 * 
	 * @param sourceTile
	 *          route start tile
	 * @param targetTile
	 *          route target tile
	 * @return list of directions to walk from the source tile to reach the target tile
	 */
	public List<Integer> shortestRoute(Tile sourceTile, Tile targetTile) {
		Integer source = graph.cell(sourceTile.col, sourceTile.row);
		Integer target = graph.cell(targetTile.col, targetTile.row);
		PathFinder<Integer> pathFinder = new BreadthFirstTraversal<>(graph, source);
		pathFinder.run();
		List<Integer> route = new ArrayList<>();
		Integer pred = null;
		for (Integer cell : pathFinder.findPath(target)) {
			if (pred != null) {
				route.add(graph.direction(pred, cell).getAsInt());
			}
			pred = cell;
		}
		return route;
	}
}