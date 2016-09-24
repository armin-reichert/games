package de.amr.grid.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.Grid2D;
import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.grid.impl.RawGrid;

public class FullGridTest {

	private static final int WIDTH = 15;
	private static final int HEIGHT = 10;
	
	private Grid2D<Integer, DefaultEdge<Integer>> grid;

	@Before
	public void setUp() {
		grid = new RawGrid(WIDTH, HEIGHT);
		grid.fillAllEdges();
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testGridSize() {
		assertEquals(grid.edgeCount(), 2 * WIDTH * HEIGHT - (WIDTH + HEIGHT));
		assertEquals(grid.vertexCount(), WIDTH * HEIGHT);
		assertEquals(grid.numCols(), WIDTH);
		assertEquals(grid.numRows(), HEIGHT);
	}

	@Test
	public void testGridEdgeIterator() {
		Iterator<DefaultEdge<Integer>> edgeIterator = grid.edgeSequence().iterator();
		for (int i = 0; i < 2 * WIDTH * HEIGHT - (WIDTH + HEIGHT); ++i) {
			assertTrue(edgeIterator.hasNext());
			edgeIterator.next();
		}
		assertFalse(edgeIterator.hasNext());
	}

	private void assertContainsExactly(Iterable<Integer> it, Integer... cells) {
		Set<Integer> set = new HashSet<>();
		for (Integer cell : cells) {
			set.add(cell);
		}
		for (Integer cell : cells) {
			assertTrue(set.contains(cell));
		}
		assertEquals(set.size(), cells.length);
	}

	@Test
	public void testAdjVertices() {
		Integer cell = grid.cell(1, 1);
		assertContainsExactly(grid.adjVertices(cell), grid.cell(1, 0), grid.cell(1, 2), grid.cell(2, 1),
				grid.cell(0, 1));
	}

	@Test
	public void testAdjVerticesAtCorners() {
		Integer cell;
		cell = grid.cell(GridPosition.TOP_LEFT);
		assertContainsExactly(grid.adjVertices(cell), grid.cell(1, 0), grid.cell(0, 1));
		cell = grid.cell(GridPosition.TOP_RIGHT);
		assertContainsExactly(grid.adjVertices(cell), grid.cell(HEIGHT - 2, 0), grid.cell(HEIGHT - 1, 1));
		cell = grid.cell(GridPosition.BOTTOM_LEFT);
		assertContainsExactly(grid.adjVertices(cell), grid.cell(1, 0), grid.cell(HEIGHT - 1, 1));
		cell = grid.cell(GridPosition.TOP_RIGHT);
		assertContainsExactly(grid.adjVertices(cell), grid.cell(HEIGHT - 2, 0), grid.cell(HEIGHT - 1, 1));
	}

	@Test
	public void testDegree() {
		assertEquals(grid.degree(grid.cell(GridPosition.TOP_LEFT)), 2);
		assertEquals(grid.degree(grid.cell(GridPosition.TOP_RIGHT)), 2);
		assertEquals(grid.degree(grid.cell(GridPosition.BOTTOM_LEFT)), 2);
		assertEquals(grid.degree(grid.cell(GridPosition.BOTTOM_RIGHT)), 2);
		for (int x = 0; x < grid.numCols(); ++x) {
			for (int y = 0; y < grid.numRows(); ++y) {
				Integer cell = grid.cell(x, y);
				assertTrue(grid.degree(cell) >= 2 && grid.degree(cell) <= 4);
				if (x == 0 || x == WIDTH - 1 || y == 0 || y == HEIGHT - 1) {
					assertTrue(grid.degree(cell) <= 3);
				}
			}
		}
	}

	@Test
	public void testConnectedTowards() {
		for (int x = 0; x < grid.numCols(); ++x) {
			for (int y = 0; y < grid.numRows(); ++y) {
				Integer cell = grid.cell(x, y);
				if (grid.numCols() > 1) {
					if (x == 0) {
						assertTrue(grid.isCellConnected(cell, Direction.E));
					}
					if (x == grid.numCols() - 1) {
						assertTrue(grid.isCellConnected(cell, Direction.W));
					}
				}
				if (grid.numRows() > 1) {
					if (y == 0) {
						assertTrue(grid.isCellConnected(cell, Direction.S));
					}
					if (y == grid.numRows() - 1) {
						assertTrue(grid.isCellConnected(cell, Direction.N));
					}
				}
			}
		}
	}
}
