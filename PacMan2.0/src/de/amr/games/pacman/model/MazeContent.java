package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Tile.EMPTY;
import static de.amr.games.pacman.model.Tile.WALL;

import java.awt.Point;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.graph.api.UndirectedEdge;
import de.amr.easy.grid.impl.GridGraph;
import de.amr.easy.grid.impl.Top4;

public class MazeContent {

	public final GridGraph<Character, Integer> grid;
	private final String mazeData;
	private final int numRows;
	private final int numCols;

	public MazeContent(String mazeData) {
		this.mazeData = mazeData;
		String[] rows = mazeData.split("\n");
		numRows = rows.length;
		numCols = rows[0].length();
		grid = new GridGraph<>(numCols, numRows, new Top4(), EMPTY, (u, v) -> 1, UndirectedEdge::new);
		grid.fill();
		grid.edges().filter(edge -> grid.get(edge.either()) == WALL || grid.get(edge.other()) == WALL)
				.forEach(grid::removeEdge);
		resetContent();
	}

	public int numCols() {
		return numCols;
	}

	public int numRows() {
		return numRows;
	}

	public Stream<Point> positions() {
		return grid.vertices().mapToObj(v -> new Point(grid.col(v), grid.row(v)));
	}

	public void resetContent() {
		String[] rows = mazeData.split("\n");
		IntStream.range(0, numRows).forEach(row -> {
			IntStream.range(0, numCols).forEach(col -> {
				grid.set(grid.cell(col, row), rows[row].charAt(col));
			});
		});
	}

	public boolean isMazeEmpty() {
		return grid.vertices().mapToObj(grid::get).filter(c -> c == Tile.PELLET || c == Tile.ENERGIZER).count() == 0;
	}

	public char getContent(int col, int row) {
		if (!grid.isValidCol(col)) {
			throw new IllegalArgumentException("Illegal column: " + col);
		}
		if (!grid.isValidRow(row)) {
			throw new IllegalArgumentException("Illegal row: " + row);
		}
		return grid.get(grid.cell(col, row));
	}

	public char getContent(Point position) {
		return getContent(position.x, position.y);
	}

	public void setContent(int col, int row, char c) {
		if (!grid.isValidCol(col)) {
			throw new IllegalArgumentException();
		}
		if (!grid.isValidRow(row)) {
			throw new IllegalArgumentException();
		}
		grid.set(grid.cell(col, row), c);
	}

	public void setContent(Point position, char c) {
		setContent(position.x, position.y, c);
	}

	public Stream<Point> positionsContaining(char content) {
		return grid.vertices().filter(v -> grid.get(v) == content).mapToObj(v -> new Point(grid.col(v), grid.row(v)));
	}

	public void print() {
		IntStream.range(0, grid.numRows()).forEach(row -> {
			IntStream.range(0, grid.numCols()).forEach(col -> {
				System.out.print(getContent(col, row));
			});
			System.out.println();
		});
	}
}