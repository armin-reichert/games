package de.amr.games.puzzle15.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.puzzle15.Puzzle15;

public class PuzzleTest {

	private Puzzle15 puzzle;

	@Before
	public void setUp() {
		puzzle = Puzzle15.ordered();
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
		assertEquals(3, Puzzle15.row(puzzle.blank()));
		assertEquals(3, Puzzle15.col(puzzle.blank()));
	}

	@Test
	public void testEquals() {
		Puzzle15 other = Puzzle15.ordered();
		assertEquals(puzzle, other);
	}

	@Test(expected = IllegalStateException.class)
	public void testMoveUp() {
		assertFalse(puzzle.canMoveUp());
		puzzle.up();
	}

	@Test()
	public void testMoveDown() {
		assertTrue(puzzle.canMoveDown());
		Puzzle15 result = puzzle.down();
		assertEquals(2, Puzzle15.row(result.blank()));
		assertEquals(3, Puzzle15.col(result.blank()));
	}

	@Test(expected = IllegalStateException.class)
	public void testMoveLeft() {
		assertFalse(puzzle.canMoveLeft());
		puzzle.left();
	}

	@Test
	public void testMoveRight() {
		assertTrue(puzzle.canMoveRight());
		Puzzle15 result = puzzle.right();
		assertEquals(3, Puzzle15.row(result.blank()));
		assertEquals(2, Puzzle15.col(result.blank()));
	}

	@Test
	public void testMoveRightThenLeft() {
		Puzzle15 result = puzzle.right().left();
		assertEquals(puzzle, result);
	}

	@Test
	public void testDownThenUp() {
		Puzzle15 result = puzzle.down().up();
		assertEquals(puzzle, result);
	}

	@Test
	public void testAroundTheWorld() {
		Puzzle15 result = puzzle.down().down().down().right().right().right().up().up().up().left().left().left();
		assertTrue(result.hasNumbers(5, 1, 2, 3, 9, 6, 7, 4, 13, 10, 11, 8, 14, 15, 12, 0));
	}

	@Test
	public void testHasNumbers() {
		assertTrue(puzzle.hasNumbers(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0));
	}
}
