package de.amr.games.pacman.board;

import static de.amr.games.pacman.board.Tile.DOOR;
import static de.amr.games.pacman.board.Tile.ENERGIZER;
import static de.amr.games.pacman.board.Tile.OUTSIDE;
import static de.amr.games.pacman.board.Tile.PELLET;
import static de.amr.games.pacman.board.Tile.WALL;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.easy.game.assets.Assets;

public class BoardTests {

	@Test
	public void testBoardLoading() {
		String data = Assets.text("board.txt");
		Board board = new Board(data);
		board.print();

		assertEquals(28, board.getGrid().numCols());
		assertEquals(36, board.getGrid().numRows());

		assertEquals(OUTSIDE, board.getTile(0, 0));
		assertEquals(WALL, board.getTile(0, 3));
		assertEquals(PELLET, board.getTile(1, 4));
		assertEquals(ENERGIZER, board.getTile(1, 6));
		assertEquals(DOOR, board.getTile(13, 15));
	}
}