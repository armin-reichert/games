package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Tile.EMPTY;
import static de.amr.games.pacman.model.Tile.WALL;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.graph.api.UndirectedEdge;
import de.amr.easy.graph.impl.traversal.AStarTraversal;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.GridGraph;
import de.amr.easy.grid.impl.Top4;

public class Maze extends GridGraph<Character, Integer> {

	public static final Topology TOPOLOGY = new Top4();

	public static Maze of(String mazeData) {
		String[] rows = mazeData.split("\n");
		return new Maze(rows[0].length(), rows.length, rows);
	}

	private final String[] contentRows;

	private Maze(int numCols, int numRows, String[] contentRows) {
		super(numCols, numRows, TOPOLOGY, EMPTY, (u, v) -> 1, UndirectedEdge::new);
		this.contentRows = contentRows;
		fill();
		reset();
		edges().filter(edge -> get(edge.either()) == WALL || get(edge.other()) == WALL).forEach(this::removeEdge);
	}

	public void reset() {
		vertices().forEach(tile -> set(tile, contentRows[row(tile)].charAt(col(tile))));
	}

	public Stream<Tile> tiles() {
		return vertices().mapToObj(v -> new Tile(col(v), row(v)));
	}

	public int cell(Tile tile) {
		return cell(tile.col, tile.row);
	}

	public char getContent(int col, int row) {
		return get(cell(col, row));
	}

	public char getContent(Tile tile) {
		return get(cell(tile.col, tile.row));
	}

	public void setContent(int col, int row, char c) {
		set(cell(col, row), c);
	}

	public void setContent(Tile tile, char c) {
		set(cell(tile.col, tile.row), c);
	}

	public List<Integer> findPath(Tile source, Tile target) {
		AStarTraversal<?> pathfinder = new AStarTraversal<>(this, this::manhattan);
		pathfinder.traverseGraph(cell(source), cell(target));
		return pathfinder.path(cell(target));
	}

	public void print() {
		IntStream.range(0, numRows()).forEach(row -> {
			IntStream.range(0, numCols()).forEach(col -> {
				System.out.print(get(cell(col, row)));
			});
			System.out.println();
		});
	}
}