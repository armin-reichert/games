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
		vertices().filter(cell -> get(cell) != WALL).forEach(cell -> {
			adj(cell).filter(neighbor -> get(neighbor) != WALL).forEach(neighbor -> {
				addEdge(cell, neighbor);
			});
		});
	}

	public void reset() {
		vertices().forEach(v -> set(v, contentRows[row(v)].charAt(col(v))));
	}

	public Stream<Tile> tiles() {
		return vertices().mapToObj(this::tile);
	}

	public boolean isValidTile(Tile tile) {
		return isValidCol(tile.col) && isValidRow(tile.row);
	}

	public int cell(Tile tile) {
		return cell(tile.col, tile.row);
	}

	public Tile tile(int cell) {
		return new Tile(col(cell), row(cell));
	}

	public char getContent(Tile tile) {
		return get(cell(tile));
	}

	public void setContent(Tile tile, char c) {
		set(cell(tile), c);
	}

	public OptionalInt direction(Tile t1, Tile t2) {
		return direction(cell(t1), cell(t2));
	}

	public List<Tile> findPath(Tile source, Tile target) {
		AStarTraversal<?> pathfinder = new AStarTraversal<>(this, this::manhattan);
		int sourceCell = cell(source), targetCell = cell(target);
		pathfinder.traverseGraph(sourceCell, targetCell);
		return pathfinder.path(targetCell).stream().map(cell -> new Tile(col(cell), row(cell)))
				.collect(Collectors.toList());
	}
}