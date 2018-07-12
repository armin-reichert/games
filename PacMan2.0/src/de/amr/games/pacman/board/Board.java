package de.amr.games.pacman.board;

import static de.amr.games.pacman.board.Tile.EMPTY;
import static de.amr.games.pacman.board.Tile.WALL;

import java.util.stream.IntStream;

import de.amr.easy.graph.api.UndirectedEdge;
import de.amr.easy.grid.impl.GridGraph;
import de.amr.easy.grid.impl.Top4;

public class Board {

	public static int TILE_SIZE = 16;

	public static int row(float y) {
		return (int) y / Board.TILE_SIZE;
	}

	public static int col(float x) {
		return (int) x / Board.TILE_SIZE;
	}

	private final GridGraph<Character, Integer> mazeGraph;
	private String[] rows;
	private final int numRows;
	private final int numCols;

	public Board(String data) {
		rows = data.split("\n");
		numRows = rows.length;
		numCols = rows[0].length();
		mazeGraph = new GridGraph<>(numCols, numRows - 5, new Top4(), EMPTY, (u, v) -> 1, UndirectedEdge::new);
		mazeGraph.fill();
		mazeGraph.edges().filter(edge -> mazeGraph.get(edge.either()) == WALL || mazeGraph.get(edge.other()) == WALL)
				.forEach(mazeGraph::removeEdge);
		resetContent();
	}

	public int getNumCols() {
		return numCols;
	}

	public int getNumRows() {
		return numRows;
	}

	public GridGraph<Character, Integer> getGrid() {
		return mazeGraph;
	}

	public void resetContent() {
		IntStream.range(3, numRows - 2).forEach(row -> {
			IntStream.range(0, numCols).forEach(col -> {
				mazeGraph.set(mazeGraph.cell(col, row - 3), rows[row].charAt(col));
			});
		});
	}

	public boolean isEmpty() {
		return mazeGraph.vertices().mapToObj(mazeGraph::get).filter(c -> c == Tile.PELLET || c == Tile.ENERGIZER)
				.count() == 0;
	}

	public char getTile(int col, int row) {
		if (!mazeGraph.isValidCol(col)) {
			throw new IllegalArgumentException("Illegal column: " + col);
		}
		if (!mazeGraph.isValidRow(row)) {
			throw new IllegalArgumentException("Illegal row: " + row);
		}
		return mazeGraph.get(mazeGraph.cell(col, row));
	}

	public void setTile(int col, int row, char c) {
		if (!mazeGraph.isValidCol(col)) {
			throw new IllegalArgumentException();
		}
		if (!mazeGraph.isValidRow(row)) {
			throw new IllegalArgumentException();
		}
		mazeGraph.set(mazeGraph.cell(col, row), c);
	}

	public void print() {
		IntStream.range(0, mazeGraph.numRows()).forEach(row -> {
			IntStream.range(0, mazeGraph.numCols()).forEach(col -> {
				System.out.print(getTile(col, row));
			});
			System.out.println();
		});
	}
}