package de.amr.games.puzzle15;

import java.util.Arrays;
import java.util.Random;

public class Puzzle {

	private final byte size;
	private final byte[] cells;
	private byte blank;

	public Puzzle(byte size) {
		this.size = size;
		int n = size * size;
		cells = new byte[n];
		for (byte i = 0; i < n; ++i) {
			cells[i] = (byte) (i + 1);
		}
		cells[n - 1] = 0;
		blank = (byte) (n - 1);
	}

	public Puzzle(Puzzle other) {
		size = other.size;
		cells = Arrays.copyOf(other.cells, other.cells.length);
		blank = other.blank;
	}

	public boolean isSolved() {
		return hasNumbers(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blank;
		result = prime * result + Arrays.hashCode(cells);
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
		if (blank != other.blank)
			return false;
		if (!Arrays.equals(cells, other.cells))
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	public byte size() {
		return size;
	}

	public byte blank() {
		return blank;
	}

	public byte get(int row, int col) {
		return cells[row * size + col];
	}

	public int col(byte i) {
		return i % size;
	}

	public int row(byte i) {
		return i / size;
	}

	public Puzzle move(Dir dir) {
		switch (dir) {
		case DOWN:
			return down();
		case LEFT:
			return left();
		case RIGHT:
			return right();
		case UP:
			return up();
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Moves tile below blank tile up.
	 * 
	 * @return resulting puzzle
	 */
	public Puzzle up() {
		if (row(blank) == size - 1) {
			throw new IllegalStateException();
		}
		return move((byte) (blank + size));
	}

	public Puzzle down() {
		if (row(blank) == 0) {
			throw new IllegalStateException();
		}
		return move((byte) (blank - size));
	}

	public Puzzle left() {
		if (col(blank) == size - 1) {
			throw new IllegalStateException();
		}
		return move((byte) (blank + 1));
	}

	public Puzzle right() {
		if (col(blank) == 0) {
			throw new IllegalStateException();
		}
		return move((byte) (blank - 1));
	}

	private Puzzle move(byte index) {
		Puzzle result = new Puzzle(this);
		result.blank = (byte) (index);
		result.cells[result.blank] = 0;
		result.cells[blank] = cells[result.blank];
		return result;
	}

	public boolean hasNumbers(int... numbers) {
		if (numbers.length != size * size) {
			throw new IllegalArgumentException();
		}
		for (int i = 0; i < numbers.length; ++i) {
			if (cells[i] != numbers[i]) {
				return false;
			}
		}
		return true;
	}

	public Puzzle shuffle(int numMoves) {
		Puzzle result = new Puzzle(this);
		Random rnd = new Random();
		Dir[] dirs = Dir.values();
		int shuffled = 0;
		while (shuffled < numMoves) {
			Dir dir = dirs[rnd.nextInt(4)];
			try {
				result = result.move(dir);
				++shuffled;
			} catch (IllegalStateException e) {
				// ignore
			}
		}
		return result;
	}

	public void print() {
		for (int row = 0; row < size; ++row) {
			for (int col = 0; col < size; ++col) {
				byte i = cells[row * size + col];
				System.out.print(i == 0 ? "   " : String.format("%02d ", i));
			}
			System.out.println();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < size; ++row) {
			for (int col = 0; col < size; ++col) {
				byte i = cells[row * size + col];
				sb.append(i == 0 ? "   " : String.format("%02d ", i));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}