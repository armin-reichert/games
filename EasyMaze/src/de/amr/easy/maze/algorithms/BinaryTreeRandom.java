package de.amr.easy.maze.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;

public class BinaryTreeRandom<Cell> extends BinaryTree<Cell> {

	private final List<Cell> cellsInRandomOrder;

	public BinaryTreeRandom(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
		cellsInRandomOrder = new ArrayList<>(grid.vertexCount());
		for (Cell cell : grid.vertexSequence()) {
			cellsInRandomOrder.add(cell);
		}
		Collections.shuffle(cellsInRandomOrder);
	}

	@Override
	protected Iterable<Cell> getCells() {
		return cellsInRandomOrder;
	}
}
