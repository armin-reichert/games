package de.amr.games.puzzle15.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.puzzle15.Puzzle;

public class PuzzleTest {

	private Puzzle puzzle;

	@Before
	public void setUp() {
		puzzle = new Puzzle(4);
	}

	@Test
	public void testConstructor() {
		assertEquals(1, puzzle.get(0, 0));
		assertEquals(2, puzzle.get(0, 1));
		assertEquals(3, puzzle.get(0, 2));
		assertEquals(4, puzzle.get(0, 3));
		assertEquals(5, puzzle.get(1, 0));
		assertEquals(6, puzzle.get(1, 1));
		assertEquals(7, puzzle.get(1, 2));
		assertEquals(8, puzzle.get(1, 3));
		assertEquals(9, puzzle.get(2, 0));
		assertEquals(10, puzzle.get(2, 1));
		assertEquals(11, puzzle.get(2, 2));
		assertEquals(12, puzzle.get(2, 3));
		assertEquals(13, puzzle.get(3, 0));
		assertEquals(14, puzzle.get(3, 1));
		assertEquals(15, puzzle.get(3, 2));
		assertEquals(0, puzzle.get(3, 3));
		assertEquals(3, puzzle.row(puzzle.blank()));
		assertEquals(3, puzzle.col(puzzle.blank()));
	}

	@Test
	public void testEquals() {
		Puzzle copy = new Puzzle(puzzle);
		assertEquals(puzzle, copy);
	}

	@Test(expected = IllegalStateException.class)
	public void testMoveUp() {
		puzzle.up();
	}

	@Test()
	public void testMoveDown() {
		Puzzle result = puzzle.down();
		assertEquals(2, result.row(result.blank()));
		assertEquals(3, result.col(result.blank()));
	}

	@Test(expected = IllegalStateException.class)
	public void testMoveLeft() {
		puzzle.left();
	}

	@Test
	public void testMoveRight() {
		Puzzle result = puzzle.right();
		assertEquals(3, result.row(result.blank()));
		assertEquals(2, result.col(result.blank()));
	}

	@Test
	public void testMoveRightThenLeft() {
		Puzzle result = puzzle.right().left();
		assertEquals(puzzle, result);
	}

	@Test
	public void testDownThenUp() {
		Puzzle result = puzzle.down().up();
		assertEquals(puzzle, result);
	}

	@Test
	public void testAroundTheWorld() {
		Puzzle result = puzzle.down().down().down().right().right().right().up().up().up().left().left().left();
		assertTrue(result.hasNumbers(5, 1, 2, 3, 9, 6, 7, 4, 13, 10, 11, 8, 14, 15, 12, 0));
	}

	@Test
	public void testHasNumbers() {
		assertTrue(puzzle.hasNumbers(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0));
	}
}
