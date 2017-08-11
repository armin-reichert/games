package de.amr.games.muehle.test;

import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;
import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertTrue;

import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.amr.games.muehle.board.BoardModel;
import de.amr.games.muehle.board.Direction;

public class BoardModelTest {

	private BoardModel emptyBoard;
	private BoardModel board;

	private static boolean equalElements(IntStream stream, IntStream other) {
		return stream.boxed().collect(toSet()).equals(other.boxed().collect(toSet()));
	}

	@Before
	public void setUp() {
		emptyBoard = new BoardModel();
		board = new BoardModel();
	}

	@After
	public void cleanUp() {
		board.clear();
	}

	@Test
	public void testCreation() {
		assertTrue(BoardModel.NUM_POS == emptyBoard.positions().count());
		assertTrue(0 == emptyBoard.stoneCount());
	}

	@Test
	public void testNeighborSymmetry() {
		emptyBoard.positions().forEach(p -> {
			Stream.of(Direction.values()).forEach(dir -> {
				OptionalInt q = emptyBoard.neighbor(p, dir);
				if (q.isPresent()) {
					assertTrue(emptyBoard.neighbor(q.getAsInt(), dir.opposite()).getAsInt() == p);
				}
			});
		});
	}

	@Test
	public void testDist2Positions() {
		assertTrue(equalElements(emptyBoard.distance2Positions(13), IntStream.of(2, 4, 8, 17, 19, 23)));
	}

	@Test
	public void testNodeDegree() {
		IntStream.of(0, 2, 23, 21, 3, 5, 20, 18, 6, 8, 17, 15)
				.forEach(p -> assertTrue(2 == emptyBoard.neighbors(p).count()));
		IntStream.of(1, 14, 22, 9, 7, 12, 16, 11).forEach(p -> assertTrue(3 == emptyBoard.neighbors(p).count()));
		IntStream.of(4, 13, 19, 10).forEach(p -> assertTrue(4 == emptyBoard.neighbors(p).count()));
	}

	@Test
	public void testHorizontalNeighbors() {
		assertTrue(emptyBoard.neighbor(1, WEST).isPresent());
		assertTrue(emptyBoard.neighbor(1, EAST).isPresent());
		assertTrue(emptyBoard.neighbor(4, WEST).isPresent());
		assertTrue(emptyBoard.neighbor(4, EAST).isPresent());
		assertTrue(emptyBoard.neighbor(7, WEST).isPresent());
		assertTrue(emptyBoard.neighbor(7, EAST).isPresent());
		assertTrue(emptyBoard.neighbor(10, WEST).isPresent());
		assertTrue(emptyBoard.neighbor(10, EAST).isPresent());
		assertTrue(emptyBoard.neighbor(13, WEST).isPresent());
		assertTrue(emptyBoard.neighbor(13, EAST).isPresent());
		assertTrue(emptyBoard.neighbor(16, WEST).isPresent());
		assertTrue(emptyBoard.neighbor(16, EAST).isPresent());
		assertTrue(emptyBoard.neighbor(19, WEST).isPresent());
		assertTrue(emptyBoard.neighbor(19, EAST).isPresent());
		assertTrue(emptyBoard.neighbor(22, WEST).isPresent());
		assertTrue(emptyBoard.neighbor(22, EAST).isPresent());

		assertTrue(emptyBoard.neighbor(1, WEST).getAsInt() == 0);
		assertTrue(emptyBoard.neighbor(1, EAST).getAsInt() == 2);
		assertTrue(emptyBoard.neighbor(4, WEST).getAsInt() == 3);
		assertTrue(emptyBoard.neighbor(4, EAST).getAsInt() == 5);
		assertTrue(emptyBoard.neighbor(7, WEST).getAsInt() == 6);
		assertTrue(emptyBoard.neighbor(7, EAST).getAsInt() == 8);
		assertTrue(emptyBoard.neighbor(10, WEST).getAsInt() == 9);
		assertTrue(emptyBoard.neighbor(10, EAST).getAsInt() == 11);
		assertTrue(emptyBoard.neighbor(13, WEST).getAsInt() == 12);
		assertTrue(emptyBoard.neighbor(13, EAST).getAsInt() == 14);
		assertTrue(emptyBoard.neighbor(16, WEST).getAsInt() == 15);
		assertTrue(emptyBoard.neighbor(16, EAST).getAsInt() == 17);
		assertTrue(emptyBoard.neighbor(19, WEST).getAsInt() == 18);
		assertTrue(emptyBoard.neighbor(19, EAST).getAsInt() == 20);
		assertTrue(emptyBoard.neighbor(22, WEST).getAsInt() == 21);
		assertTrue(emptyBoard.neighbor(22, EAST).getAsInt() == 23);
	}

	@Test
	public void testVerticalNeighbors() {
		assertTrue(emptyBoard.neighbor(9, NORTH).isPresent());
		assertTrue(emptyBoard.neighbor(9, SOUTH).isPresent());
		assertTrue(emptyBoard.neighbor(10, NORTH).isPresent());
		assertTrue(emptyBoard.neighbor(10, SOUTH).isPresent());
		assertTrue(emptyBoard.neighbor(11, NORTH).isPresent());
		assertTrue(emptyBoard.neighbor(11, SOUTH).isPresent());
		assertTrue(emptyBoard.neighbor(4, NORTH).isPresent());
		assertTrue(emptyBoard.neighbor(4, SOUTH).isPresent());
		assertTrue(emptyBoard.neighbor(19, NORTH).isPresent());
		assertTrue(emptyBoard.neighbor(19, SOUTH).isPresent());
		assertTrue(emptyBoard.neighbor(12, NORTH).isPresent());
		assertTrue(emptyBoard.neighbor(12, SOUTH).isPresent());
		assertTrue(emptyBoard.neighbor(13, NORTH).isPresent());
		assertTrue(emptyBoard.neighbor(13, SOUTH).isPresent());
		assertTrue(emptyBoard.neighbor(14, NORTH).isPresent());
		assertTrue(emptyBoard.neighbor(14, SOUTH).isPresent());

		assertTrue(emptyBoard.neighbor(9, NORTH).getAsInt() == 0);
		assertTrue(emptyBoard.neighbor(9, SOUTH).getAsInt() == 21);
		assertTrue(emptyBoard.neighbor(10, NORTH).getAsInt() == 3);
		assertTrue(emptyBoard.neighbor(10, SOUTH).getAsInt() == 18);
		assertTrue(emptyBoard.neighbor(11, NORTH).getAsInt() == 6);
		assertTrue(emptyBoard.neighbor(11, SOUTH).getAsInt() == 15);
		assertTrue(emptyBoard.neighbor(4, NORTH).getAsInt() == 1);
		assertTrue(emptyBoard.neighbor(4, SOUTH).getAsInt() == 7);
		assertTrue(emptyBoard.neighbor(19, NORTH).getAsInt() == 16);
		assertTrue(emptyBoard.neighbor(19, SOUTH).getAsInt() == 22);
		assertTrue(emptyBoard.neighbor(12, NORTH).getAsInt() == 8);
		assertTrue(emptyBoard.neighbor(12, SOUTH).getAsInt() == 17);
		assertTrue(emptyBoard.neighbor(13, NORTH).getAsInt() == 5);
		assertTrue(emptyBoard.neighbor(13, SOUTH).getAsInt() == 20);
		assertTrue(emptyBoard.neighbor(14, NORTH).getAsInt() == 2);
		assertTrue(emptyBoard.neighbor(14, SOUTH).getAsInt() == 23);
	}

	@Test
	public void testClear() {
		assertTrue(board.positions(WHITE).count() + board.positions(BLACK).count() == 0);
		board.positions().forEach(p -> board.putStoneAt(p, WHITE));
		assertTrue(board.positions(WHITE).count() == BoardModel.NUM_POS);
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
		board.putStoneAt(0, WHITE);
		board.putStoneAt(1, WHITE);
		board.putStoneAt(2, WHITE);
		assertTrue(IntStream.of(0, 1, 2).allMatch(p -> board.isPositionInHorizontalMill(p, WHITE)));
		assertTrue(IntStream.range(3, 24).noneMatch(p -> board.isPositionInHorizontalMill(p, WHITE)));
	}

	@Test
	public void testVerticalMill() {
		board.putStoneAt(3, WHITE);
		board.putStoneAt(10, WHITE);
		board.putStoneAt(18, WHITE);
		assertTrue(IntStream.of(3, 10, 18).allMatch(p -> board.isPositionInVerticalMill(p, WHITE)));
		assertTrue(IntStream.range(0, 24).filter(p -> !(p == 3 || p == 10 || p == 18))
				.noneMatch(p -> board.isPositionInVerticalMill(p, WHITE)));
	}

	@Test
	public void testTwoMillsLater() {
		board.putStoneAt(13, WHITE);
		assertTrue(equalElements(board.positionsOpeningTwoMillsLater(WHITE), IntStream.of(2, 4, 8, 17, 19, 23)));
	}
}