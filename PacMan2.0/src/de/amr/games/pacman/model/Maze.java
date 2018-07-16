package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Tile.EMPTY;
import static de.amr.games.pacman.model.Tile.WALL;

import java.awt.Point;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.graph.api.UndirectedEdge;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.GridGraph;
import de.amr.easy.grid.impl.Top4;

public class Maze extends GridGraph<Character, Integer> {

	public static final Topology TOPOLOGY = new Top4();

	private final String[] mazeData;

	public Maze(String mazeText) {
		super(28, 31, TOPOLOGY, EMPTY, (u, v) -> 1, UndirectedEdge::new);
		mazeData = mazeText.split("\n");
		fill();
		edges().filter(edge -> get(edge.either()) == WALL || get(edge.other()) == WALL).forEach(this::removeEdge);
		reset();
	}

	public void reset() {
		vertices().forEach(tile -> set(tile, mazeData[row(tile)].charAt(col(tile))));
	}

	public Stream<Point> tiles() {
		return vertices().mapToObj(v -> new Point(col(v), row(v)));
	}

	public Stream<Point> tilesContaining(char content) {
		return vertices().filter(v -> get(v) == content).mapToObj(v -> new Point(col(v), row(v)));
	}

	public boolean containsFood() {
		return vertices().anyMatch(v -> get(v) == Tile.PELLET || get(v) == Tile.ENERGIZER);
	}

	public char getContent(int col, int row) {
		return get(cell(col, row));
	}

	public char getContent(Point position) {
		return get(cell(position.x, position.y));
	}

	public void setContent(int col, int row, char c) {
		set(cell(col, row), c);
	}

	public void setContent(Point position, char c) {
		set(cell(position.x, position.y), c);
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