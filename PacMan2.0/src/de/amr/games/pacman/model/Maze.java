package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Tile.EMPTY;
import static de.amr.games.pacman.model.Tile.WALL;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
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
		reset();
		fill();
		edges().filter(edge -> get(edge.either()) == WALL || get(edge.other()) == WALL).forEach(this::removeEdge);
	}

	public void reset() {
		vertices().forEach(v -> set(v, contentRows[row(v)].charAt(col(v))));
	}

	public Stream<Tile> tiles() {
		return vertices().mapToObj(v -> new Tile(col(v), row(v)));
	}
	
	public boolean isValidTile(Tile tile) {
		return isValidCol(tile.col) && isValidRow(tile.row);
	}

	public int cell(Tile tile) {
		return cell(tile.col, tile.row);
	}

	public char getContent(Tile tile) {
		return get(cell(tile.col, tile.row));
	}

	public void setContent(Tile tile, char c) {
		set(cell(tile.col, tile.row), c);
	}

	public OptionalInt direction(Tile t1, Tile t2) {
		return direction(cell(t1.col, t1.row), cell(t2.col, t2.row));
	}

	private List<Integer> computePath(int source, int target) {
		AStarTraversal<?> pathfinder = new AStarTraversal<>(this, this::manhattan);
		pathfinder.traverseGraph(source, target);
		return pathfinder.path(target);
	}

	public List<Tile> findPath(Tile source, Tile target) {
		return computePath(cell(source.col, source.row), cell(target.col, target.col)).stream()
				.map(v -> new Tile(col(v), row(v))).collect(Collectors.toList());
	}
}