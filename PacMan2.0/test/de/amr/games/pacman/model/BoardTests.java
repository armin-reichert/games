package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.Tile.DOOR;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;
import static de.amr.games.pacman.model.Tile.WALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;

import org.junit.Test;

import de.amr.easy.game.assets.Assets;

public class BoardTests {

	@Test
	public void testBoardLoading() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		printMaze(maze);

		assertEquals(28, maze.numCols());
		assertEquals(31, maze.numRows());

		assertEquals(4, maze.tiles().filter(tile -> maze.getContent(tile) == Tile.ENERGIZER).count());
		assertEquals(240, maze.tiles().filter(tile -> maze.getContent(tile) == Tile.PELLET).count());

		assertTrue(WALL == maze.getContent(0, 3));
		assertTrue(PELLET == maze.getContent(1, 4));
		assertTrue(ENERGIZER == maze.getContent(1, 3));
		assertTrue(DOOR == maze.getContent(13, 12));
	}

	public void printMaze(Maze maze) {
		IntStream.range(0, maze.numRows()).forEach(row -> {
			IntStream.range(0, maze.numCols()).forEach(col -> {
				System.out.print(maze.getContent(col, row));
			});
			System.out.println();
		});
	}
}