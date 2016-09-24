package de.amr.grid.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Grid2D;
import de.amr.easy.grid.impl.RawGrid;

public class EmptyGridTest {

	private Grid2D<Integer, DefaultEdge<Integer>>  grid;

	@Before
	public void setUp() {
		grid = new RawGrid(0, 0);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testGridSize() {
		assertEquals(grid.edgeCount(), 0);
		assertEquals(grid.vertexCount(), 0);
		assertEquals(grid.numCols(), 0);
		assertEquals(grid.numRows(), 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGridAccessException() {
		grid.cell(0, 0);
	}

	@Test
	public void testGridVertexIterator() {
		Iterator<Integer> vertexIterator = grid.vertexSequence().iterator();
		assertFalse(vertexIterator.hasNext());
	}
	
	@Test
	public void testGridEdgeIterator() {
		Iterator<DefaultEdge<Integer>> edgeIterator = grid.edgeSequence().iterator();
		assertFalse(edgeIterator.hasNext());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGridEdgeAccess() {
		grid.edge(0, 1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGridEdgeAdd() {
		grid.addEdge(new DefaultEdge<Integer>(0, 1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGridVertexDegree() {
		grid.degree(0);
	}
}
