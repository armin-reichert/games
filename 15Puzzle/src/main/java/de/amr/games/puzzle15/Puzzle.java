package de.amr.games.puzzle15;

import java.util.Arrays;

public class Puzzle {

	public enum Dir {
		UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

		private Dir(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}

		final int dx, dy;
	}

	private final int size;
	private final int[][] cells;
	private int emptyRow, emptyCol;

	public Puzzle(int size) {
		this.size = size;
		cells = new int[size][size];
		int i = 1;
		for (int row = 0; row < size; ++row) {
			for (int col = 0; col < size; ++col) {
				cells[row][col] = i++;
			}
		}
		emptyRow = emptyCol = size - 1;
		cells[emptyRow][emptyCol] = 0;
	}

	public Puzzle(Puzzle other) {
		size = other.size;
		cells = new int[size][size];
		for (int row = 0; row < size; ++row) {
			for (int col = 0; col < size; ++col) {
				cells[row][col] = other.cells[row][col];
			}
		}
		emptyCol = other.emptyCol;
		emptyRow = other.emptyRow;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(cells);
		result = prime * result + emptyCol;
		result = prime * result + emptyRow;
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Puzzle other = (Puzzle) obj;
		if (!Arrays.deepEquals(cells, other.cells))
			return false;
		if (emptyCol != other.emptyCol)
			return false;
		if (emptyRow != other.emptyRow)
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	public int getEmptyCol() {
		return emptyCol;
	}

	public int getEmptyRow() {
		return emptyRow;
	}

	public int get(int row, int col) {
		return cells[row][col];
	}

	public Puzzle up() {
		if (emptyRow == size - 1) {
			throw new IllegalStateException();
		}
		Puzzle result = new Puzzle(this);
		result.emptyRow = emptyRow + 1;
		result.swap(emptyRow, emptyCol, result.emptyRow, result.emptyCol);
		return result;
	}

	public Puzzle down() {
		if (emptyRow == 0) {
			throw new IllegalStateException();
		}
		Puzzle result = new Puzzle(this);
		result.emptyRow = emptyRow - 1;
		result.swap(emptyRow, emptyCol, result.emptyRow, result.emptyCol);
		return result;
	}
	
	public Puzzle left() {
		if (emptyCol == size - 1) {
			throw new IllegalStateException();
		}
		Puzzle result = new Puzzle(this);
		result.emptyCol = emptyCol + 1;
		result.swap(emptyRow, emptyCol, result.emptyRow, result.emptyCol);
		return result;
	}
	
	public Puzzle right() {
		if (emptyCol == 0) {
			throw new IllegalStateException();
		}
		Puzzle result = new Puzzle(this);
		result.emptyCol = emptyCol - 1;
		result.swap(emptyRow, emptyCol, result.emptyRow, result.emptyCol);
		return result;
	}
	
	public void swap(int r, int c, int rr, int cc) {
		int tmp = cells[r][c];
		cells[r][c] = cells[rr][cc];
		cells[rr][cc] = tmp;
	}

	public void print() {
		for (int row = 0; row < size; ++row) {
			for (int col = 0; col < size; ++col) {
				int i = cells[row][col];
				System.out.print(i == 0 ? "   " : String.format("%02d ", i));
			}
			System.out.println();
		}
	}
}