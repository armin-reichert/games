package de.amr.games.puzzle15.model;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class Puzzle15 {

	private static final byte[] ORDERED_SEQ = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0 };

	public static final Puzzle15 ORDERED = new Puzzle15(ORDERED_SEQ);

	private final byte[] cells;

	private Puzzle15(int[] seq) {
		cells = new byte[16];
		for (byte i = 0; i < 16; ++i) {
			cells[i] = (byte) seq[i];
		}
	}

	private Puzzle15(byte[] seq) {
		cells = new byte[16];
		for (byte i = 0; i < 16; ++i) {
			cells[i] = seq[i];
		}
	}

	private Puzzle15(Puzzle15 other) {
		this(other.cells);
	}

	public static Puzzle15 of(int... cells) {
		return new Puzzle15(cells);
	}

	public static Puzzle15 ordered() {
		return new Puzzle15(ORDERED_SEQ);
	}

	public static Puzzle15 shuffled() {
		Puzzle15 puzzle = new Puzzle15(ORDERED_SEQ);
		shuffleArray(puzzle.cells);
		return puzzle;
	}

	public static Puzzle15 randomMoves(int numMoves) {
		Puzzle15 puzzle = new Puzzle15(ORDERED_SEQ);
		int moves = 0;
		while (moves < numMoves) {
			Dir dir = Dir.values()[new Random().nextInt(4)];
			if (puzzle.canMove(dir)) {
				puzzle = puzzle.move(dir);
				moves++;
			}
		}
		return puzzle;
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

	public boolean isOrdered() {
		return Arrays.equals(cells, ORDERED_SEQ);
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

	public static int col(byte index) {
		return index % 4;
	}

	public static int row(byte index) {
		return index / 4;
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
		return move((byte) (blank() + 4), this::canMoveUp);
	}

	public Puzzle15 down() {
		return move((byte) (blank() - 4), this::canMoveDown);
	}

	public Puzzle15 left() {
		return move((byte) (blank() + 1), this::canMoveLeft);
	}

	public Puzzle15 right() {
		return move((byte) (blank() - 1), this::canMoveRight);
	}

	private Puzzle15 move(byte index, BooleanSupplier precondition) {
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
		for (byte i = 0; i < numbers.length; ++i) {
			if (cells[i] != numbers[i]) {
				return false;
			}
		}
		return true;
	}

	// Durstenfeld shuffle
	private static void shuffleArray(byte[] array) {
		Random rnd = ThreadLocalRandom.current();
		for (int i = array.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			byte a = array[index];
			array[index] = array[i];
			array[i] = a;
		}
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (int row = 0; row < 4; ++row) {
			for (int col = 0; col < 4; ++col) {
				byte i = cells[row * 4 + col];
				sb.append(i == 0 ? "   " : String.format("%02d ", i));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * 
	 * <p>
	 * If N is even, puzzle instance is solvable if
	 * <ul>
	 * <li>the blank is on an even row counting from the bottom (second-last, fourth-last, etc.) and
	 * number of inversions is odd.
	 * <li>the blank is on an odd row counting from the bottom (last, third-last, fifth-last, etc.) and
	 * number of inversions is even.
	 * </ul>
	 * 
	 * @see https://www.geeksforgeeks.org/check-instance-15-puzzle-solvable/
	 */
	public boolean isSolvable() {
		int numInversions = numInversions(), row = 4 - row(blank());
		return even(row) != even(numInversions);
	}

	private boolean even(int n) {
		return n % 2 == 0;
	}

	public int numInversions() {
		int cnt = 0;
		for (int i = 0; i < 15; ++i) {
			for (int j = i + 1; j < 16; ++j) {
				if (cells[i] != 0 && cells[j] != 0 && cells[i] > cells[j]) {
					++cnt;
				}
			}
		}
		return cnt;
	}

}