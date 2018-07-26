package de.amr.games.pacman.model;

import java.awt.Dimension;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.easy.graph.api.GraphTraversal;
import de.amr.easy.graph.api.UndirectedEdge;
import de.amr.easy.graph.impl.traversal.AStarTraversal;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.GridGraph;
import de.amr.easy.grid.impl.Top4;

public class Maze {

	public static final Topology TOPOLOGY = new Top4();

	public Tile pacManHome, blinkyHome, pinkyHome, inkyHome, clydeHome, infoTile;

	private final String data;
	private final GridGraph<Character, Integer> graph;

	public Maze(String data) {
		this.data = data;
		Dimension size = parseDimension();
		graph = new GridGraph<>(size.width, size.height, TOPOLOGY, Tile.EMPTY, (u, v) -> 1,
				UndirectedEdge::new);
		parseContent();
	}

	public void reset() {
		parseContent();
	}

	private Dimension parseDimension() {
		String[] rows = data.split("\n");
		return new Dimension(rows[0].length(), rows.length);
	}

	private void parseContent() {
		Dimension size = parseDimension();
		String[] rows = data.split("\n");
		for (int row = 0; row < size.height; ++row) {
			for (int col = 0; col < size.width; ++col) {
				char c = rows[row].charAt(col);
				if (c == Tile.POS_BLINKY) {
					blinkyHome = new Tile(col, row);
				} else if (c == Tile.POS_PINKY) {
					pinkyHome = new Tile(col, row);
				} else if (c == Tile.POS_INKY) {
					inkyHome = new Tile(col, row);
				} else if (c == Tile.POS_CLYDE) {
					clydeHome = new Tile(col, row);
				} else if (c == Tile.POS_INFO) {
					infoTile = new Tile(col, row);
				} else if (c == Tile.POS_PACMAN) {
					pacManHome = new Tile(col, row);
				} else {
					graph.set(graph.cell(col, row), c);
				}
			}
		}
		graph.fill();
		graph.edges()
				.filter(e -> graph.get(e.either()) == Tile.WALL || graph.get(e.other()) == Tile.WALL)
				.forEach(graph::removeEdge);
	}

	public GridGraph<Character, Integer> getGraph() {
		return graph;
	}

	public int numCols() {
		return graph.numCols();
	}

	public int numRows() {
		return graph.numRows();
	}

	public Stream<Tile> tiles() {
		return graph.vertices().mapToObj(this::tile);
	}

	public boolean isValidTile(Tile tile) {
		return graph.isValidCol(tile.col) && graph.isValidRow(tile.row);
	}

	public char getContent(int col, int row) {
		return graph.get(graph.cell(col, row));
	}

	public char getContent(Tile tile) {
		return graph.get(cell(tile));
	}

	public void setContent(Tile tile, char c) {
		graph.set(cell(tile), c);
	}

	public OptionalInt direction(Tile t1, Tile t2) {
		return graph.direction(cell(t1), cell(t2));
	}

	public boolean hasAdjacentTile(Tile t1, Tile t2) {
		return graph.adjacent(cell(t1), cell(t2));
	}

	public Optional<Tile> neighborTile(Tile tile, int dir) {
		OptionalInt neighbor = graph.neighbor(cell(tile), dir);
		return neighbor.isPresent() ? Optional.of(tile(neighbor.getAsInt())) : Optional.empty();
	}

	public Stream<Tile> getAdjacentTiles(Tile tile) {
		return graph.adj(cell(tile)).mapToObj(this::tile);
	}

	public List<Tile> findPath(Tile source, Tile target) {
		GraphTraversal pathfinder = new AStarTraversal<>(graph, graph::manhattan);
		pathfinder.traverseGraph(cell(source), cell(target));
		return pathfinder.path(cell(target)).stream().map(this::tile).collect(Collectors.toList());
	}

	public OptionalInt dirAlongPath(List<Tile> path) {
		return path.size() < 2 ? OptionalInt.empty() : direction(path.get(0), path.get(1));
	}

	// convert between vertex numbers ("cells") and tiles

	private int cell(Tile tile) {
		return graph.cell(tile.col, tile.row);
	}

	private Tile tile(int cell) {
		return new Tile(graph.col(cell), graph.row(cell));
	}

}