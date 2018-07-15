package de.amr.games.pacman.board;

import static de.amr.games.pacman.board.Tile.DOOR;
import static de.amr.games.pacman.board.Tile.ENERGIZER;
import static de.amr.games.pacman.board.Tile.PELLET;
import static de.amr.games.pacman.board.Tile.WALL;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.easy.game.assets.Assets;

public class BoardTests {

	@Test
	public void testBoardLoading() {
		String data = Assets.text("maze.txt");
		MazeContent board = new MazeContent(data);
		board.print();

		assertEquals(28, board.grid.numCols());
		assertEquals(31, board.grid.numRows());

		assertEquals(WALL, board.getContent(0, 3));
		assertEquals(PELLET, board.getContent(1, 4));
		assertEquals(ENERGIZER, board.getContent(1, 3));
		assertEquals(DOOR, board.getContent(13, 12));
	}
}