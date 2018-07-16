package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Tile.DOOR;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;
import static de.amr.games.pacman.model.Tile.WALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.amr.easy.game.assets.Assets;

public class BoardTests {

	@Test
	public void testBoardLoading() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		maze.print();

		assertEquals(28, maze.numCols());
		assertEquals(31, maze.numRows());

		assertTrue(WALL == maze.get(maze.cell(0, 3)));
		assertTrue(PELLET == maze.get(maze.cell(1, 4)));
		assertTrue(ENERGIZER == maze.get(maze.cell(1, 3)));
		assertTrue(DOOR == maze.get(maze.cell(13, 12)));
	}
}