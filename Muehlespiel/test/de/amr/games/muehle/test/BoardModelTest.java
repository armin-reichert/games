package de.amr.games.muehle.test;

import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;
import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.amr.games.muehle.board.BoardGraph;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Direction;

public class BoardModelTest {

	private Board board;

	private static boolean sameElements(IntStream stream, IntStream other) {
		return stream.boxed().collect(toSet()).equals(other.boxed().collect(toSet()));
	}

	// Test environment

	@Before
	public void setUp() {
		board = new Board();
	}

	@After
	public void cleanUp() {
	}

	// Graph tests

	@Test
	public void testCreation() {
		assertTrue(BoardGraph.NUM_POS == board.positions().count());
	}

	@Test
	public void testNeighborSymmetry() {
		board.positions().forEach(p -> {
			Stream.of(Direction.values()).forEach(dir -> {
				board.neighbor(p, dir).ifPresent(q -> {
					assertTrue(board.neighbor(q, dir.opposite()).getAsInt() == p);
				});
			});
		});
	}

	@Test
	public void testNextToNeighborPositions() {
		assertTrue(sameElements(board.nextToNeighbors(0), IntStream.of(2, 4, 10, 21)));
		assertTrue(sameElements(board.nextToNeighbors(6), IntStream.of(4, 8, 10, 15)));
		assertTrue(sameElements(board.nextToNeighbors(13), IntStream.of(2, 4, 8, 17, 19, 23)));
	}

	@Test
	public void testNodeDegree() {
		IntStream.of(0, 2, 23, 21, 3, 5, 20, 18, 6, 8, 17, 15).forEach(p -> assertTrue(2 == board.neighbors(p).count()));
		IntStream.of(1, 14, 22, 9, 7, 12, 16, 11).forEach(p -> assertTrue(3 == board.neighbors(p).count()));
		IntStream.of(4, 13, 19, 10).forEach(p -> assertTrue(4 == board.neighbors(p).count()));
	}

	@Test
	public void testHorizontalNeighbors() {
		assertTrue(board.neighbor(1, WEST).isPresent());
		assertTrue(board.neighbor(1, EAST).isPresent());
		assertTrue(board.neighbor(4, WEST).isPresent());
		assertTrue(board.neighbor(4, EAST).isPresent());
		assertTrue(board.neighbor(7, WEST).isPresent());
		assertTrue(board.neighbor(7, EAST).isPresent());
		assertTrue(board.neighbor(10, WEST).isPresent());
		assertTrue(board.neighbor(10, EAST).isPresent());
		assertTrue(board.neighbor(13, WEST).isPresent());
		assertTrue(board.neighbor(13, EAST).isPresent());
		assertTrue(board.neighbor(16, WEST).isPresent());
		assertTrue(board.neighbor(16, EAST).isPresent());
		assertTrue(board.neighbor(19, WEST).isPresent());
		assertTrue(board.neighbor(19, EAST).isPresent());
		assertTrue(board.neighbor(22, WEST).isPresent());
		assertTrue(board.neighbor(22, EAST).isPresent());

		assertTrue(board.neighbor(1, WEST).getAsInt() == 0);
		assertTrue(board.neighbor(1, EAST).getAsInt() == 2);
		assertTrue(board.neighbor(4, WEST).getAsInt() == 3);
		assertTrue(board.neighbor(4, EAST).getAsInt() == 5);
		assertTrue(board.neighbor(7, WEST).getAsInt() == 6);
		assertTrue(board.neighbor(7, EAST).getAsInt() == 8);
		assertTrue(board.neighbor(10, WEST).getAsInt() == 9);
		assertTrue(board.neighbor(10, EAST).getAsInt() == 11);
		assertTrue(board.neighbor(13, WEST).getAsInt() == 12);
		assertTrue(board.neighbor(13, EAST).getAsInt() == 14);
		assertTrue(board.neighbor(16, WEST).getAsInt() == 15);
		assertTrue(board.neighbor(16, EAST).getAsInt() == 17);
		assertTrue(board.neighbor(19, WEST).getAsInt() == 18);
		assertTrue(board.neighbor(19, EAST).getAsInt() == 20);
		assertTrue(board.neighbor(22, WEST).getAsInt() == 21);
		assertTrue(board.neighbor(22, EAST).getAsInt() == 23);
	}

	@Test
	public void testVerticalNeighbors() {
		assertTrue(board.neighbor(9, NORTH).isPresent());
		assertTrue(board.neighbor(9, SOUTH).isPresent());
		assertTrue(board.neighbor(10, NORTH).isPresent());
		assertTrue(board.neighbor(10, SOUTH).isPresent());
		assertTrue(board.neighbor(11, NORTH).isPresent());
		assertTrue(board.neighbor(11, SOUTH).isPresent());
		assertTrue(board.neighbor(4, NORTH).isPresent());
		assertTrue(board.neighbor(4, SOUTH).isPresent());
		assertTrue(board.neighbor(19, NORTH).isPresent());
		assertTrue(board.neighbor(19, SOUTH).isPresent());
		assertTrue(board.neighbor(12, NORTH).isPresent());
		assertTrue(board.neighbor(12, SOUTH).isPresent());
		assertTrue(board.neighbor(13, NORTH).isPresent());
		assertTrue(board.neighbor(13, SOUTH).isPresent());
		assertTrue(board.neighbor(14, NORTH).isPresent());
		assertTrue(board.neighbor(14, SOUTH).isPresent());

		assertTrue(board.neighbor(9, NORTH).getAsInt() == 0);
		assertTrue(board.neighbor(9, SOUTH).getAsInt() == 21);
		assertTrue(board.neighbor(10, NORTH).getAsInt() == 3);
		assertTrue(board.neighbor(10, SOUTH).getAsInt() == 18);
		assertTrue(board.neighbor(11, NORTH).getAsInt() == 6);
		assertTrue(board.neighbor(11, SOUTH).getAsInt() == 15);
		assertTrue(board.neighbor(4, NORTH).getAsInt() == 1);
		assertTrue(board.neighbor(4, SOUTH).getAsInt() == 7);
		assertTrue(board.neighbor(19, NORTH).getAsInt() == 16);
		assertTrue(board.neighbor(19, SOUTH).getAsInt() == 22);
		assertTrue(board.neighbor(12, NORTH).getAsInt() == 8);
		assertTrue(board.neighbor(12, SOUTH).getAsInt() == 17);
		assertTrue(board.neighbor(13, NORTH).getAsInt() == 5);
		assertTrue(board.neighbor(13, SOUTH).getAsInt() == 20);
		assertTrue(board.neighbor(14, NORTH).getAsInt() == 2);
		assertTrue(board.neighbor(14, SOUTH).getAsInt() == 23);
	}

	// Model tests

	@Test
	public void testClear() {
		assertTrue(board.positions(WHITE).count() + board.positions(BLACK).count() == 0);
		board.positions().forEach(p -> board.putStoneAt(p, WHITE));
		assertTrue(board.positions(WHITE).count() == BoardGraph.NUM_POS);
		assertTrue(board.positions(BLACK).count() == 0);
		board.clear();
		assertTrue(board.positions(WHITE).count() == 0);
		assertTrue(board.positions(BLACK).count() == 0);
	}

	@Test
	public void testTrapped() {
		board.putStoneAt(0, WHITE);
		board.putStoneAt(1, BLACK);
		board.putStoneAt(9, BLACK);
		assertTrue(board.isTrapped(WHITE));
		assertTrue(!board.hasEmptyNeighbor(0));
	}

	@Test
	public void testHorizontalMill() {
		assertTrue(board.areHMillPositions(0, 1, 2));
		board.putStoneAt(0, WHITE);
		board.putStoneAt(1, WHITE);
		board.putStoneAt(2, WHITE);
		assertTrue(IntStream.of(0, 1, 2).allMatch(p -> board.inHMill(p, WHITE)));
		assertTrue(IntStream.range(3, 24).noneMatch(p -> board.inHMill(p, WHITE)));
		assertTrue(board.hasHMill(0, 1, 2, WHITE));
	}

	@Test
	public void testVerticalMill() {
		assertTrue(board.areVMillPositions(3, 10, 18));
		board.putStoneAt(3, WHITE);
		board.putStoneAt(10, WHITE);
		board.putStoneAt(18, WHITE);
		assertTrue(IntStream.of(3, 10, 18).allMatch(p -> board.inVMill(p, WHITE)));
		assertTrue(IntStream.range(0, 24).filter(p -> !(p == 3 || p == 10 || p == 18))
				.noneMatch(p -> board.inVMill(p, WHITE)));
		assertTrue(board.hasVMill(3, 10, 18, WHITE));
	}

	@Test
	public void testTwoMillsLater() {
		board.putStoneAt(13, WHITE);
		assertTrue(sameElements(board.positionsOpeningTwoMillsLater(WHITE), IntStream.of(2, 4, 8, 17, 19, 23)));
	}

	@Test
	public void testMillClosingPositions() {
		board.putStoneAt(9, WHITE);
		board.putStoneAt(21, WHITE);
		board.putStoneAt(11, WHITE);
		board.putStoneAt(23, WHITE);
		board.positions().forEach(p -> {
			assertTrue((p == 0 || p == 10 || p == 22) == board.isMillClosingPosition(p, WHITE));
		});
		board.putStoneAt(3, WHITE);
		assertTrue(board.isMillClosedByMove(3, 10, WHITE));
		board.putStoneAt(18, WHITE);
		assertTrue(board.isMillClosedByMove(18, 10, WHITE));
		board.putStoneAt(19, WHITE);
		assertTrue(board.isMillClosedByMove(19, 22, WHITE));
	}

}