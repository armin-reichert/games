package de.amr.easy.maze.algorithms;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;

import java.util.Random;
import java.util.function.Consumer;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableDataGrid2D;

/**
 * Creates a random binary spanning tree.
 * 
 * @author Armin Reichert
 */
public class BinaryTree<Cell> implements Consumer<Cell> {

	protected final ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid;
	private final Random rnd = new Random();

	public BinaryTree(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		this.grid = grid;
	}

	@Override
	public void accept(Cell start) {
		for (Cell cell : getCells()) {
			Cell neighbor = getRandomNeighbor(cell, Direction.S, Direction.E);
			if (neighbor != null) {
				grid.addEdge(new DefaultEdge<Cell>(cell, neighbor));
				grid.setContent(cell, COMPLETED);
				grid.setContent(neighbor, COMPLETED);
			}
		}
	}

	private Cell getRandomNeighbor(Cell cell, Direction d1, Direction d2) {
		boolean b = rnd.nextBoolean();
		Cell neighbor = grid.neighbor(cell, b ? d1 : d2);
		return neighbor != null ? neighbor : grid.neighbor(cell, b ? d2 : d1);
	}

	/*
	 * Can be overriden by subclass to specify different cell iteration.
	 */
	protected Iterable<Cell> getCells() {
		return grid.vertexSequence();
	}
}
