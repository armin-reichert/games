package de.amr.games.puzzle15;

import java.util.Arrays;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class Puzzle15 {

	private static final byte[] ORDERED = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0 };

	private final byte[] cells;

	public Puzzle15() {
		cells = Arrays.copyOf(ORDERED, 16);
	}

	public Puzzle15(Puzzle15 other) {
		cells = Arrays.copyOf(other.cells, 16);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(cells);
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
		Puzzle15 other = (Puzzle15) obj;
		if (!Arrays.equals(cells, other.cells))
			return false;
		return true;
	}

	public boolean isSolved() {
		return Arrays.equals(cells, ORDERED);
	}

	public byte blank() {
		for (byte i = 0; i < cells.length; ++i) {
			if (cells[i] == 0) {
				return i;
			}
		}
		throw new IllegalStateException();
	}

	public byte get(int row, int col) {
		return cells[row * 4 + col];
	}

	public int col(byte i) {
		return i % 4;
	}

	public int row(byte i) {
		return i / 4;
	}

	public Puzzle15 move(Dir dir) {
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

	public boolean canMoveUp() {
		return row(blank()) < 3;
	}

	public boolean canMoveDown() {
		return row(blank()) > 0;
	}

	public boolean canMoveLeft() {
		return col(blank()) < 3;
	}

	public boolean canMoveRight() {
		return col(blank()) > 0;
	}

	public Puzzle15 up() {
		return swapBlankCellWith((byte) (blank() + 4), this::canMoveUp);
	}

	public Puzzle15 down() {
		return swapBlankCellWith((byte) (blank() - 4), this::canMoveDown);
	}

	public Puzzle15 left() {
		return swapBlankCellWith((byte) (blank() + 1), this::canMoveLeft);
	}

	public Puzzle15 right() {
		return swapBlankCellWith((byte) (blank() - 1), this::canMoveRight);
	}

	private Puzzle15 swapBlankCellWith(byte index, BooleanSupplier precondition) {
		if (precondition.getAsBoolean()) {
			Puzzle15 result = new Puzzle15(this);
			result.cells[index] = 0;
			result.cells[blank()] = cells[index];
			return result;
		}
		throw new IllegalStateException();
	}

	public boolean hasNumbers(int... numbers) {
		if (numbers.length != cells.length) {
			throw new IllegalArgumentException();
		}
		for (int i = 0; i < numbers.length; ++i) {
			if (cells[i] != numbers[i]) {
				return false;
			}
		}
		return true;
	}

	public Puzzle15 shuffle(int numMoves) {
		Puzzle15 result = new Puzzle15(this);
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

	public boolean canMove(Dir dir) {
		switch (dir) {
		case DOWN:
			return canMoveDown();
		case LEFT:
			return canMoveLeft();
		case RIGHT:
			return canMoveRight();
		case UP:
			return canMoveUp();
		}
		throw new IllegalArgumentException();
	}

	public Stream<Dir> possibleMoveDirs() {
		return Stream.of(Dir.values()).filter(this::canMove);
	}

	public void println() {
		System.out.println(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < 4; ++row) {
			for (int col = 0; col < 4; ++col) {
				byte i = cells[row * 4 + col];
				sb.append(i == 0 ? "   " : String.format("%02d ", i));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}