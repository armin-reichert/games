package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Tile.EMPTY;
import static de.amr.games.pacman.model.Tile.WALL;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.easy.graph.api.GraphTraversal;
import de.amr.easy.graph.api.UndirectedEdge;
import de.amr.easy.graph.impl.traversal.AStarTraversal;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.GridGraph;
import de.amr.easy.grid.impl.Top4;

public class Maze extends GridGraph<Character, Integer> {

	public static final Topology TOPOLOGY = new Top4();

	public static final Tile PACMAN_HOME = new Tile(14, 23);
	public static final Tile BLINKY_HOME = new Tile(13, 11);
	public static final Tile PINKY_HOME = new Tile(13, 14);
	public static final Tile INKY_HOME = new Tile(11, 14);
	public static final Tile CLYDE_HOME = new Tile(15, 14);
	public static final Tile BONUS_TILE = new Tile(13, 17);

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
		edges().filter(e -> get(e.either()) == WALL || get(e.other()) == WALL).forEach(this::removeEdge);
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

	public Stream<Tile> getAdjacentTiles(Tile tile) {
		return adj(cell(tile)).mapToObj(this::tile);
	}

	public List<Tile> findPath(Tile source, Tile target) {
		GraphTraversal pathfinder = new AStarTraversal<>(this, this::manhattan);
		pathfinder.traverseGraph(cell(source), cell(target));
		return pathfinder.path(cell(target)).stream().map(this::tile).collect(Collectors.toList());
	}

	public OptionalInt alongPath(List<Tile> path) {
		return path.size() < 2 ? OptionalInt.empty() : direction(path.get(0), path.get(1));
	}
}