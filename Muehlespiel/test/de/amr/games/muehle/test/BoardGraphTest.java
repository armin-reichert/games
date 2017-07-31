package de.amr.games.muehle.test;

import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;
import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import de.amr.games.muehle.board.BoardGraph;
import de.amr.games.muehle.board.Direction;

public class BoardGraphTest {

	private BoardGraph board;
	private BoardGraph filledBoard;

	@Before
	public void setUp() {
		board = new BoardGraph();
		filledBoard = new BoardGraph();
	}

	@Test
	public void testCreation() {
		assertTrue(BoardGraph.NUM_POS == board.positions().count());
		assertTrue(0 == board.stones().count());
	}

	@Test
	public void testNeighborSymmetry() {
		board.positions().forEach(p -> {
			Stream.of(Direction.values()).forEach(dir -> {
				int q = board.neighbor(p, dir);
				assertTrue(q == -1 || board.neighbor(q, dir.opposite()) == p);
			});
		});
	}

	@Test
	public void testNodeDegree() {
		IntStream.of(0, 2, 23, 21, 3, 5, 20, 18, 6, 8, 17, 15).forEach(p -> assertTrue(2 == board.neighbors(p).count()));
		IntStream.of(1, 14, 22, 9, 7, 12, 16, 11).forEach(p -> assertTrue(3 == board.neighbors(p).count()));
		IntStream.of(4, 13, 19, 10).forEach(p -> assertTrue(4 == board.neighbors(p).count()));
	}

	@Test
	public void testHorizontalNeighbors() {
		assertTrue(board.neighbor(1, WEST) == 0);
		assertTrue(board.neighbor(1, EAST) == 2);
		assertTrue(board.neighbor(4, WEST) == 3);
		assertTrue(board.neighbor(4, EAST) == 5);
		assertTrue(board.neighbor(7, WEST) == 6);
		assertTrue(board.neighbor(7, EAST) == 8);
		assertTrue(board.neighbor(10, WEST) == 9);
		assertTrue(board.neighbor(10, EAST) == 11);
		assertTrue(board.neighbor(13, WEST) == 12);
		assertTrue(board.neighbor(13, EAST) == 14);
		assertTrue(board.neighbor(16, WEST) == 15);
		assertTrue(board.neighbor(16, EAST) == 17);
		assertTrue(board.neighbor(19, WEST) == 18);
		assertTrue(board.neighbor(19, EAST) == 20);
		assertTrue(board.neighbor(22, WEST) == 21);
		assertTrue(board.neighbor(22, EAST) == 23);
	}

	@Test
	public void testVerticalNeighbors() {
		assertTrue(board.neighbor(9, NORTH) == 0);
		assertTrue(board.neighbor(9, SOUTH) == 21);
		assertTrue(board.neighbor(10, NORTH) == 3);
		assertTrue(board.neighbor(10, SOUTH) == 18);
		assertTrue(board.neighbor(11, NORTH) == 6);
		assertTrue(board.neighbor(11, SOUTH) == 15);
		assertTrue(board.neighbor(4, NORTH) == 1);
		assertTrue(board.neighbor(4, SOUTH) == 7);
		assertTrue(board.neighbor(19, NORTH) == 16);
		assertTrue(board.neighbor(19, SOUTH) == 22);
		assertTrue(board.neighbor(12, NORTH) == 8);
		assertTrue(board.neighbor(12, SOUTH) == 17);
		assertTrue(board.neighbor(13, NORTH) == 5);
		assertTrue(board.neighbor(13, SOUTH) == 20);
		assertTrue(board.neighbor(14, NORTH) == 2);
		assertTrue(board.neighbor(14, SOUTH) == 23);
	}

}
