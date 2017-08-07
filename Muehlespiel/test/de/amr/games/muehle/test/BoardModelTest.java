package de.amr.games.muehle.test;

import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;
import static de.amr.games.muehle.board.StoneType.BLACK;
import static de.amr.games.muehle.board.StoneType.WHITE;
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

	private BoardModel board;
	private BoardModel filledBoard;

	@Before
	public void setUp() {
		board = new BoardModel();
		filledBoard = new BoardModel();
	}

	@After
	public void cleanUp() {
		filledBoard.clear();
	}

	@Test
	public void testCreation() {
		assertTrue(BoardModel.NUM_POS == board.positions().count());
		assertTrue(0 == board.stoneCount());
	}

	@Test
	public void testNeighborSymmetry() {
		board.positions().forEach(p -> {
			Stream.of(Direction.values()).forEach(dir -> {
				OptionalInt q = board.neighbor(p, dir);
				if (q.isPresent()) {
					assertTrue(board.neighbor(q.getAsInt(), dir.opposite()).getAsInt() == p);
				}
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

	@Test
	public void testTrapped() {
		filledBoard.putStoneAt(0, WHITE);
		filledBoard.putStoneAt(1, BLACK);
		filledBoard.putStoneAt(9, BLACK);
		assertTrue(filledBoard.isTrapped(WHITE));
		assertTrue(!filledBoard.hasEmptyNeighbor(0));
	}

	@Test
	public void testHorizontalMill() {
		filledBoard.putStoneAt(0, WHITE);
		filledBoard.putStoneAt(1, WHITE);
		filledBoard.putStoneAt(2, WHITE);
		assertTrue(IntStream.of(0, 1, 2).allMatch(p -> filledBoard.isPositionInsideMill(p, WHITE)));
		assertTrue(IntStream.range(3, 24).noneMatch(p -> filledBoard.isPositionInsideMill(p, WHITE)));
	}

	@Test
	public void testVerticalMill() {
		filledBoard.putStoneAt(3, WHITE);
		filledBoard.putStoneAt(10, WHITE);
		filledBoard.putStoneAt(18, WHITE);
		assertTrue(IntStream.of(3, 10, 18).allMatch(p -> filledBoard.isPositionInsideMill(p, WHITE)));
		assertTrue(IntStream.range(0, 24).filter(p -> !(p == 3 || p == 10 || p == 18))
				.noneMatch(p -> filledBoard.isPositionInsideMill(p, WHITE)));
	}

}
