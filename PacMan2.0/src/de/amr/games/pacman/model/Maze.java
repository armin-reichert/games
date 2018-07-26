package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Tile.EMPTY;
import static de.amr.games.pacman.model.Tile.WALL;

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

	public static Maze of(String mazeData) {
		String[] rows = mazeData.split("\n");
		return new Maze(rows[0].length(), rows.length, rows);
	}

	private GridGraph<Character, Integer> graph;
	private final String[] content;
	public Tile pacManHome, blinkyHome, pinkyHome, inkyHome, clydeHome, infoTile;

	private Maze(int numCols, int numRows, String[] content) {
		graph = new GridGraph<>(numCols, numRows, TOPOLOGY, EMPTY, (u, v) -> 1, UndirectedEdge::new);
		this.content = content;
		loadContent();
		for (int row = 0; row < numRows; ++row) {
			for (int col = 0; col < numCols; ++col) {
				char c = getOriginalContent(row, col);
				if (c == Tile.BLINKY) {
					blinkyHome = new Tile(col, row);
				} else if (c == Tile.PINKY) {
					pinkyHome = new Tile(col, row);
				} else if (c == Tile.INKY) {
					inkyHome = new Tile(col, row);
				} else if (c == Tile.CLYDE) {
					clydeHome = new Tile(col, row);
				} else if (c == Tile.INFO) {
					infoTile = new Tile(col, row);
				} else if (c == Tile.PACMAN) {
					pacManHome = new Tile(col, row);
				}
			}
		}
		graph.fill();
		graph.edges().filter(e -> graph.get(e.either()) == WALL || graph.get(e.other()) == WALL)
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

	public void loadContent() {
		graph.vertices().forEach(v -> graph.set(v, content[graph.row(v)].charAt(graph.col(v))));
	}

	public Stream<Tile> tiles() {
		return graph.vertices().mapToObj(this::tile);
	}

	public boolean isValidTile(Tile tile) {
		return graph.isValidCol(tile.col) && graph.isValidRow(tile.row);
	}

	public int cell(Tile tile) {
		return graph.cell(tile.col, tile.row);
	}

	public Tile tile(int cell) {
		return new Tile(graph.col(cell), graph.row(cell));
	}

	private char getOriginalContent(int row, int col) {
		return content[row].charAt(col);
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

	public boolean adjacent(Tile t1, Tile t2) {
		return graph.adjacent(cell(t1), cell(t2));
	}

	public Optional<Tile> neighbor(Tile tile, int dir) {
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
}